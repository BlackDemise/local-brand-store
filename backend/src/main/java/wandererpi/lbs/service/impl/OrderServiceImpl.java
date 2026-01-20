package wandererpi.lbs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.config.PaymentConfig;
import wandererpi.lbs.dto.request.CancelOrderRequest;
import wandererpi.lbs.dto.request.PlaceOrderRequest;
import wandererpi.lbs.dto.request.UpdateOrderStatusRequest;
import wandererpi.lbs.dto.response.OrderHistoryResponse;
import wandererpi.lbs.dto.response.OrderItemResponse;
import wandererpi.lbs.dto.response.OrderResponse;
import wandererpi.lbs.dto.response.OrderSummaryResponse;
import wandererpi.lbs.entity.*;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.enums.PaymentMethod;
import wandererpi.lbs.enums.ReservationStatus;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.*;
import wandererpi.lbs.service.EmailService;
import wandererpi.lbs.service.OrderService;
import wandererpi.lbs.util.VietQRUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final PaymentConfig paymentConfig;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ReservationRepository reservationRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final SkuRepository skuRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        log.info("Placing order for cart ID: {}", request.getCartId());
        
        // 1. Get cart
        Cart cart = cartRepository.findById(request.getCartId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        
        // 2. Get active reservations
        List<Reservation> reservations = reservationRepository
            .findByCartIdAndStatus(cart.getId(), ReservationStatus.ACTIVE);
        
        if (reservations.isEmpty()) {
            throw new ApplicationException(ErrorCode.NO_ACTIVE_RESERVATION);
        }
        
        // 3. Validate reservations not expired
        Instant now = Instant.now();
        for (Reservation reservation : reservations) {
            if (reservation.getExpiresAt().isBefore(now)) {
                throw new ApplicationException(ErrorCode.RESERVATION_EXPIRED);
            }
        }
        
        // 4. Create order
        Order order = Order.builder()
            .trackingToken(generateTrackingToken())
            .status(determineOrderStatus(request.getPaymentMethod()))
            .paymentMethod(request.getPaymentMethod())
            .customerName(request.getCustomerName())
            .customerPhone(request.getCustomerPhone())
            .customerEmail(request.getCustomerEmail())
            .shippingAddr(request.getShippingAddress())
            .note(request.getNote())
            .build();
        
        // Calculate total from reservations
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Reservation reservation : reservations) {
            Sku sku = skuRepository.findById(reservation.getSku().getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SKU_NOT_FOUND));
            totalAmount = totalAmount.add(
                sku.getPrice().multiply(BigDecimal.valueOf(reservation.getQuantity()))
            );
        }
        order.setTotalAmount(totalAmount);
        
        order = orderRepository.save(order);
        log.info("Order created with ID: {} and tracking token: {}", order.getId(), order.getTrackingToken());
        
        // 5. Create order items from reservations
        for (Reservation reservation : reservations) {
            Sku sku = reservation.getSku();
            
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .sku(sku)
                .quantity(reservation.getQuantity())
                .unitPrice(sku.getPrice())  // Price snapshot
                .build();
            
            orderItemRepository.save(orderItem);
            
            // 6. Mark reservation as CONSUMED
            reservation.setStatus(ReservationStatus.CONSUMED);
            reservationRepository.save(reservation);
        }
        
        // 7. Clear cart
        cartItemRepository.deleteByCartId(cart.getId());
        log.info("Cart cleared for cart ID: {}", cart.getId());
        
        // 8. Create order history entry
        OrderHistory history = OrderHistory.builder()
            .order(order)
            .oldStatus(null)
            .newStatus(order.getStatus().name())
            .note("Order created")
            .build();
        orderHistoryRepository.save(history);
        
        log.info("Order placement completed for order ID: {}", order.getId());
        
        // 9. Send confirmation email (async)
        emailService.sendOrderConfirmation(order);

        OrderResponse response = mapToOrderResponse(order);

        // If BANK_TRANSFER, add payment info
        if (order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            response.setBankTransferInfo(generateBankTransferInfo(order));
        }

        return response;
    }

    @Override
    public OrderResponse getOrderByTrackingToken(String trackingToken) {
        Order order = orderRepository.findByTrackingToken(trackingToken)
            .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        return mapToOrderResponse(order);
    }

    @Override
    public Page<OrderSummaryResponse> getOrders(OrderStatus status, Instant startDate, 
                                                 Instant endDate, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            orders = orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        
        return orders.map(this::mapToOrderSummaryResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String adminEmail) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getNewStatus();
        
        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);
        
        // Update order status
        order.setStatus(newStatus);
        order = orderRepository.save(order);
        
        // Create history entry
        OrderHistory history = OrderHistory.builder()
            .order(order)
            .oldStatus(oldStatus.name())
            .newStatus(newStatus.name())
            .note(request.getNote() != null ? request.getNote() : "Status updated by admin")
            .build();
        // Send status update email (async)
        emailService.sendOrderStatusUpdate(order, oldStatus, newStatus);
        
        orderHistoryRepository.save(history);
        
        log.info("Order {} status updated from {} to {} by {}", orderId, oldStatus, newStatus, adminEmail);
        
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request, String adminEmail) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        
        OrderStatus currentStatus = order.getStatus();
        
        // Validate cancellation is allowed
        if (currentStatus == OrderStatus.SHIPPING || currentStatus == OrderStatus.DELIVERED) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        
        // Restore stock
        restoreStockForOrder(orderId);
        
        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        
        // Create history entry
        OrderHistory history = OrderHistory.builder()
            .order(order)
            .oldStatus(currentStatus.name())
            .newStatus(OrderStatus.CANCELLED.name())
            .note("Order cancelled: " + request.getReason())
            .build();
        orderHistoryRepository.save(history);
        
        log.info("Order {} cancelled by {}. Reason: {}", orderId, adminEmail, request.getReason());
        
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderHistoryResponse> getOrderHistory(Long orderId) {
        // Verify order exists
        if (!orderRepository.existsById(orderId)) {
            throw new ApplicationException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        List<OrderHistory> histories = orderHistoryRepository.findByOrderIdOrderByUpdatedAtDesc(orderId);
        return histories.stream()
            .map(this::mapToOrderHistoryResponse)
            .collect(Collectors.toList());
    }

    // ========== Private Helper Methods ==========
    
    private OrderStatus determineOrderStatus(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.COD 
            ? OrderStatus.CONFIRMED 
            : OrderStatus.PENDING_PAYMENT;
    }
    
    private String generateTrackingToken() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid transitions
        Map<OrderStatus, Set<OrderStatus>> validTransitions = new HashMap<>();
        validTransitions.put(OrderStatus.PENDING_PAYMENT, 
            Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        validTransitions.put(OrderStatus.CONFIRMED, 
            Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED));
        validTransitions.put(OrderStatus.SHIPPING, 
            Set.of(OrderStatus.DELIVERED));
        validTransitions.put(OrderStatus.DELIVERED, 
            Set.of());  // Terminal state
        validTransitions.put(OrderStatus.CANCELLED, 
            Set.of());  // Terminal state
        
        Set<OrderStatus> allowedTransitions = validTransitions.get(currentStatus);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }
    
    private void restoreStockForOrder(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        
        for (OrderItem item : items) {
            // Atomic stock restoration
            int updated = jdbcTemplate.update(
                "UPDATE skus SET stock_qty = stock_qty + ? WHERE id = ?",
                item.getQuantity(),
                item.getSku().getId()
            );
            
            if (updated > 0) {
                log.info("Restored {} units of SKU {} for cancelled order {}", 
                    item.getQuantity(), item.getSku().getId(), orderId);
            }
        }
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        
        List<OrderItemResponse> itemResponses = orderItems.stream()
            .map(this::mapToOrderItemResponse)
            .collect(Collectors.toList());
        
        return OrderResponse.builder()
            .orderId(order.getId())
            .trackingToken(order.getTrackingToken())
            .status(order.getStatus())
            .paymentMethod(order.getPaymentMethod())
            .totalAmount(order.getTotalAmount())
            .customerName(order.getCustomerName())
            .customerPhone(order.getCustomerPhone())
            .customerEmail(order.getCustomerEmail())
            .shippingAddress(order.getShippingAddr())
            .note(order.getNote())
            .items(itemResponses)
            .createdAt(order.getCreatedAt())
            .build();
    }
    
    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        Sku sku = item.getSku();
        Product product = sku.getProduct();
        
        return OrderItemResponse.builder()
            .id(item.getId())
            .skuId(sku.getId())
            .skuCode(sku.getPrimarySkuCode())
            .productName(product.getName())
            .size(sku.getSize())
            .color(sku.getColor())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .itemTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .build();
    }
    
    private OrderSummaryResponse mapToOrderSummaryResponse(Order order) {
        return OrderSummaryResponse.builder()
            .orderId(order.getId())
            .trackingToken(order.getTrackingToken())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .customerName(order.getCustomerName())
            .createdAt(order.getCreatedAt())
            .build();
    }
    
    private OrderHistoryResponse mapToOrderHistoryResponse(OrderHistory history) {
        return OrderHistoryResponse.builder()
            .id(history.getId())
            .oldStatus(history.getOldStatus())
            .newStatus(history.getNewStatus())
            .note(history.getNote())
            .changedAt(history.getUpdatedAt())
            .build();
    }

    /**
     * Generate bank transfer information with QR code
     */
    private OrderResponse.BankTransferInfo generateBankTransferInfo(Order order) {
        String transferMessage = "ORDER-" + order.getId();
        String qrContent = VietQRUtil.generateVietQR(
                paymentConfig.getBankTransfer().getBankCode(),
                paymentConfig.getBankTransfer().getAccountNo(),
                order.getTotalAmount().longValue(),
                transferMessage
        );

        return OrderResponse.BankTransferInfo.builder()
                .qrContent(qrContent)
                .bankCode(paymentConfig.getBankTransfer().getBankCode())
                .accountNo(paymentConfig.getBankTransfer().getAccountNo())
                .accountName(paymentConfig.getBankTransfer().getAccountName())
                .amount(order.getTotalAmount().longValue())
                .transferMessage(transferMessage)
                .build();
    }
}
