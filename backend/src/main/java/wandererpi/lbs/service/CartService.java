package wandererpi.lbs.service;

import wandererpi.lbs.dto.request.AddToCartRequest;
import wandererpi.lbs.dto.request.UpdateCartItemRequest;
import wandererpi.lbs.dto.response.CartResponse;

public interface CartService {
    
    /**
     * Get or create cart by token
     */
    CartResponse getOrCreateCart(String cartToken);
    
    /**
     * Add item to cart with stock validation
     */
    CartResponse addToCart(AddToCartRequest request);
    
    /**
     * Update cart item quantity
     */
    CartResponse updateCartItem(String cartToken, Long cartItemId, UpdateCartItemRequest request);
    
    /**
     * Remove item from cart
     */
    CartResponse removeCartItem(String cartToken, Long cartItemId);
    
    /**
     * Get cart with real-time stock validation
     */
    CartResponse getCart(String cartToken);
    
    /**
     * Clear cart
     */
    void clearCart(String cartToken);
    
    /**
     * Generate unique cart token
     */
    String generateCartToken();
}
