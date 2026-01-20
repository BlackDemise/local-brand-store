package wandererpi.lbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.dto.request.AddToCartRequest;
import wandererpi.lbs.dto.request.UpdateCartItemRequest;
import wandererpi.lbs.dto.response.CartItemResponse;
import wandererpi.lbs.dto.response.CartResponse;
import wandererpi.lbs.dto.response.StockWarning;
import wandererpi.lbs.entity.Cart;
import wandererpi.lbs.entity.CartItem;
import wandererpi.lbs.entity.Product;
import wandererpi.lbs.entity.Sku;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.CartItemRepository;
import wandererpi.lbs.repository.CartRepository;
import wandererpi.lbs.repository.SkuRepository;
import wandererpi.lbs.service.CartService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final SkuRepository skuRepository;

    @Override
    @Transactional
    public CartResponse getOrCreateCart(String cartToken) {
        if (cartToken == null || cartToken.isEmpty()) {
            // Create new cart
            cartToken = generateCartToken();
            Cart cart = new Cart();
            cart.setToken(cartToken);
            cart = cartRepository.save(cart);
            
            return CartResponse.builder()
                .cartId(cart.getId())
                .cartToken(cart.getToken())
                .items(new ArrayList<>())
                .subtotal(BigDecimal.ZERO)
                .warnings(new ArrayList<>())
                .build();
        }
        
        // Try to get existing cart
        String finalCartToken = cartToken;
        Cart cart = cartRepository.findByToken(cartToken)
            .orElseGet(() -> {
                // Create new cart if not found
                Cart newCart = new Cart();
                newCart.setToken(finalCartToken);
                return cartRepository.save(newCart);
            });
        
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        // Validate SKU exists
        Sku sku = skuRepository.findById(request.getSkuId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.SKU_NOT_FOUND));
        
        // Get or create cart
        String cartToken = request.getCartToken();
        if (cartToken == null || cartToken.isEmpty()) {
            cartToken = generateCartToken();
        }

        String finalCartToken = cartToken;
        Cart cart = cartRepository.findByToken(cartToken)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setToken(finalCartToken);
                return cartRepository.save(newCart);
            });
        
        // Check if item already in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndSkuId(cart.getId(), sku.getId())
            .orElse(null);
        
        Integer requestedQty = request.getQuantity();
        Integer finalQty;
        
        if (cartItem != null) {
            // Update existing cart item (sum quantities)
            requestedQty = cartItem.getQuantity() + request.getQuantity();
        }
        
        // Adjust quantity to available stock
        finalQty = Math.min(requestedQty, sku.getStockQty());
        
        if (cartItem != null) {
            cartItem.setQuantity(finalQty);
            cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setSku(sku);
            cartItem.setQuantity(finalQty);
            cartItemRepository.save(cartItem);
        }
        
        // Build response with warnings
        CartResponse response = buildCartResponse(cart);
        
        // Add warning if quantity was adjusted
        if (finalQty < requestedQty) {
            StockWarning warning = StockWarning.builder()
                .skuId(sku.getId())
                .message("Quantity adjusted due to stock availability")
                .requestedQty(requestedQty)
                .availableQty(sku.getStockQty())
                .build();
            response.getWarnings().add(warning);
        }
        
        return response;
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String cartToken, Long cartItemId, UpdateCartItemRequest request) {
        // Get cart
        Cart cart = cartRepository.findByToken(cartToken)
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        
        // Get cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        
        // Verify cart item belongs to this cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        
        // If quantity is 0, remove item
        if (request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
            return buildCartResponse(cart);
        }
        
        // Get SKU
        Sku sku = cartItem.getSku();
        
        // Adjust quantity to available stock
        Integer requestedQty = request.getQuantity();
        Integer finalQty = Math.min(requestedQty, sku.getStockQty());
        
        cartItem.setQuantity(finalQty);
        cartItemRepository.save(cartItem);
        
        // Build response with warnings
        CartResponse response = buildCartResponse(cart);
        
        // Add warning if quantity was adjusted
        if (finalQty < requestedQty) {
            StockWarning warning = StockWarning.builder()
                .skuId(sku.getId())
                .message("Quantity adjusted due to stock availability")
                .requestedQty(requestedQty)
                .availableQty(sku.getStockQty())
                .build();
            response.getWarnings().add(warning);
        }
        
        return response;
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(String cartToken, Long cartItemId) {
        // Get cart
        Cart cart = cartRepository.findByToken(cartToken)
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        
        // Get cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        
        // Verify cart item belongs to this cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        
        cartItemRepository.delete(cartItem);
        
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse getCart(String cartToken) {
        if (cartToken == null || cartToken.isEmpty()) {
            throw new ApplicationException(ErrorCode.INVALID_CART_TOKEN);
        }
        
        Cart cart = cartRepository.findByToken(cartToken)
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(String cartToken) {
        Cart cart = cartRepository.findByToken(cartToken)
            .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        
        cartItemRepository.deleteByCartId(cart.getId());
    }

    @Override
    public String generateCartToken() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Build CartResponse with real-time stock validation
     */
    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        List<CartItemResponse> itemResponses = new ArrayList<>();
        List<StockWarning> warnings = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (CartItem cartItem : cartItems) {
            Sku sku = cartItem.getSku();
            Product product = sku.getProduct();
            
            // Real-time stock validation
            Integer currentStock = sku.getStockQty();
            Integer quantity = cartItem.getQuantity();
            boolean sufficient = quantity <= currentStock;
            
            // Add warning if insufficient stock
            if (!sufficient) {
                StockWarning warning = StockWarning.builder()
                    .skuId(sku.getId())
                    .message("Insufficient stock for " + product.getName() + " - " + sku.getSize() + "/" + sku.getColor())
                    .requestedQty(quantity)
                    .availableQty(currentStock)
                    .build();
                warnings.add(warning);
            }
            
            BigDecimal itemTotal = sku.getPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(itemTotal);
            
            CartItemResponse itemResponse = CartItemResponse.builder()
                .id(cartItem.getId())
                .skuId(sku.getId())
                .skuCode(sku.getPrimarySkuCode())
                .productName(product.getName())
                .size(sku.getSize())
                .color(sku.getColor())
                .unitPrice(sku.getPrice())
                .quantity(quantity)
                .availableStock(currentStock)
                .sufficient(sufficient)
                .itemTotal(itemTotal)
                .build();
            
            itemResponses.add(itemResponse);
        }
        
        return CartResponse.builder()
            .cartId(cart.getId())
            .cartToken(cart.getToken())
            .items(itemResponses)
            .subtotal(subtotal)
            .warnings(warnings)
            .build();
    }
}
