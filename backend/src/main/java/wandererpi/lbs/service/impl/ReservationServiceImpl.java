package wandererpi.lbs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.dto.request.StartCheckoutRequest;
import wandererpi.lbs.dto.response.CheckoutSessionResponse;
import wandererpi.lbs.dto.response.ReservationResponse;
import wandererpi.lbs.entity.Cart;
import wandererpi.lbs.entity.CartItem;
import wandererpi.lbs.entity.Reservation;
import wandererpi.lbs.entity.Sku;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.enums.ReservationStatus;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.CartItemRepository;
import wandererpi.lbs.repository.CartRepository;
import wandererpi.lbs.repository.ReservationRepository;
import wandererpi.lbs.repository.SkuRepository;
import wandererpi.lbs.service.ReservationService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    
    private static final int RESERVATION_EXPIRATION_SECONDS = 900;
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ReservationRepository reservationRepository;
    private final SkuRepository skuRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CheckoutSessionResponse startCheckout(StartCheckoutRequest request) {
        log.info("Starting checkout for cart token: {}", request.getCartToken());
        
        // 1. Get cart
        Cart cart = cartRepository.findByToken(request.getCartToken())
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        
        // 2. Get all cart items and create a map for quick lookup
        List<CartItem> allCartItems = cartItemRepository.findByCartId(cart.getId());
        Map<Long, CartItem> cartItemMap = allCartItems.stream()
            .collect(Collectors.toMap(CartItem::getId, item -> item));
        
        // 3. Validate all requested items exist in cart and belong to this cart
        List<CartItem> itemsToCheckout = new ArrayList<>();
        for (StartCheckoutRequest.CheckoutItem checkoutItem : request.getItems()) {
            CartItem cartItem = cartItemMap.get(checkoutItem.getCartItemId());
            if (cartItem == null) {
                throw new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND);
            }
            
            // Validate requested quantity doesn't exceed cart quantity
            if (checkoutItem.getQuantity() > cartItem.getQuantity()) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST);
            }
            
            itemsToCheckout.add(cartItem);
        }
        
        if (itemsToCheckout.isEmpty()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }
        
        // 4. Check for existing active reservations
        List<Reservation> existingReservations = reservationRepository
            .findByCartIdAndStatus(cart.getId(), ReservationStatus.ACTIVE);
        
        if (!existingReservations.isEmpty()) {
            // If there are active reservations, check if they're expired
            Instant now = Instant.now();
            boolean allExpired = existingReservations.stream()
                .allMatch(r -> r.getExpiresAt().isBefore(now));
            
            if (!allExpired) {
                // Return existing checkout session
                log.info("Cart {} already has active reservations", cart.getId());
                return buildCheckoutSessionResponse(cart, existingReservations);
            } else {
                // Expire old reservations and create new ones
                log.info("Expiring old reservations for cart {}", cart.getId());
                releaseReservations(existingReservations);
            }
        }
        
        // 5. Calculate expiration time
        Instant expiresAt = Instant.now().plusSeconds(RESERVATION_EXPIRATION_SECONDS);
        
        // 6. Create reservations for selected items (atomic stock reservation)
        List<ReservationResponse> reservationResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Map checkout items by cart item ID for quantity lookup
        Map<Long, Integer> quantityMap = request.getItems().stream()
            .collect(Collectors.toMap(
                StartCheckoutRequest.CheckoutItem::getCartItemId,
                StartCheckoutRequest.CheckoutItem::getQuantity
            ));
        
        for (CartItem cartItem : itemsToCheckout) {
            Sku sku = cartItem.getSku();
            Integer quantity = quantityMap.get(cartItem.getId());
            
            // ATOMIC OPERATION: Reserve stock
            Reservation reservation = reserveStock(cart, sku, quantity, expiresAt);
            
            // Calculate amount
            totalAmount = totalAmount.add(sku.getPrice().multiply(BigDecimal.valueOf(quantity)));
            
            // Build response
            ReservationResponse reservationResponse = ReservationResponse.builder()
                .reservationId(reservation.getId())
                .skuId(sku.getId())
                .skuCode(sku.getPrimarySkuCode())
                .quantity(quantity)
                .status(ReservationStatus.ACTIVE)
                .expiresAt(expiresAt)
                .build();
            
            reservationResponses.add(reservationResponse);
            
            log.info("Reserved {} units of SKU {} for cart {}", quantity, sku.getPrimarySkuCode(), cart.getId());
        }
        
        log.info("Checkout started successfully for cart {}. Total: {}", cart.getId(), totalAmount);
        
        return CheckoutSessionResponse.builder()
            .cartId(cart.getId())
            .reservations(reservationResponses)
            .expiresAt(expiresAt)
            .expirationSeconds(RESERVATION_EXPIRATION_SECONDS)
            .totalAmount(totalAmount)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public void validateReservation(Long cartId) {
        List<Reservation> reservations = reservationRepository
            .findByCartIdAndStatus(cartId, ReservationStatus.ACTIVE);
        
        if (reservations.isEmpty()) {
            throw new ApplicationException(ErrorCode.NO_ACTIVE_RESERVATION);
        }
        
        Instant now = Instant.now();
        for (Reservation reservation : reservations) {
            if (reservation.getExpiresAt().isBefore(now)) {
                throw new ApplicationException(ErrorCode.RESERVATION_EXPIRED);
            }
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int releaseExpiredReservations() {
        log.info("Starting expired reservations cleanup...");
        
        Instant now = Instant.now();
        List<Reservation> expiredReservations = reservationRepository
            .findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);
        
        int count = releaseReservations(expiredReservations);
        
        log.info("Released {} expired reservations", count);
        
        return count;
    }
    
    /**
     * CRITICAL METHOD: Atomic stock reservation
     * This is THE solution to the race condition problem
     * <br/>
     * Uses atomic UPDATE statement with WHERE condition to check and decrement stock
     * in a single database operation, preventing race conditions even for the "last item"
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    protected Reservation reserveStock(Cart cart, Sku sku, Integer quantity, Instant expiresAt) {
        // ATOMIC OPERATION: Check and decrement in single UPDATE
        // This prevents race conditions at the database level
        int rowsAffected = jdbcTemplate.update(
            "UPDATE skus SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?",
            quantity, sku.getId(), quantity
        );
        
        // If no rows affected, stock was insufficient
        if (rowsAffected == 0) {
            log.error("Insufficient stock for SKU {}. Requested: {}, Available: {}", 
                sku.getPrimarySkuCode(), quantity, sku.getStockQty());
            
            throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK);
        }
        
        // Stock successfully decremented - create reservation record
        Reservation reservation = new Reservation();
        reservation.setCart(cart);
        reservation.setSku(sku);
        reservation.setQuantity(quantity);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setExpiresAt(expiresAt);
        
        return reservationRepository.save(reservation);
    }
    
    /**
     * Release reservations and restore stock
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    protected int releaseReservations(List<Reservation> reservations) {
        int count = 0;
        
        for (Reservation reservation : reservations) {
            // Restore stock (atomic operation)
            jdbcTemplate.update(
                "UPDATE skus SET stock_qty = stock_qty + ? WHERE id = ?",
                reservation.getQuantity(),
                reservation.getSku().getId()
            );
            
            // Mark reservation as expired
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            
            count++;
            
            log.debug("Released reservation {} for SKU {}, restored {} units",
                reservation.getId(), reservation.getSku().getId(), reservation.getQuantity());
        }
        
        return count;
    }
    
    /**
     * Build checkout session response from existing reservations
     */
    private CheckoutSessionResponse buildCheckoutSessionResponse(Cart cart, List<Reservation> reservations) {
        List<ReservationResponse> reservationResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Instant expiresAt = null;
        
        for (Reservation reservation : reservations) {
            Sku sku = skuRepository.findById(reservation.getSku().getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SKU_NOT_FOUND));
            
            totalAmount = totalAmount.add(sku.getPrice().multiply(BigDecimal.valueOf(reservation.getQuantity())));

            if (expiresAt == null || reservation.getExpiresAt().isAfter(expiresAt)) {
                expiresAt = reservation.getExpiresAt();
            }

            ReservationResponse response = ReservationResponse.builder()
                .reservationId(reservation.getId())
                .skuId(sku.getId())
                .skuCode(sku.getPrimarySkuCode())
                .quantity(reservation.getQuantity())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .build();
            
            reservationResponses.add(response);
        }
        
        return CheckoutSessionResponse.builder()
            .cartId(cart.getId())
            .reservations(reservationResponses)
            .expiresAt(expiresAt)
            .expirationSeconds(RESERVATION_EXPIRATION_SECONDS)
            .totalAmount(totalAmount)
            .build();
    }
}
