package wandererpi.lbs.resource.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wandererpi.lbs.dto.request.AddToCartRequest;
import wandererpi.lbs.dto.request.UpdateCartItemRequest;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.dto.response.CartResponse;
import wandererpi.lbs.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartResource {
    
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<String, CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        
        CartResponse response = cartService.addToCart(request);
        
        return ResponseEntity.ok(
            ApiResponse.<String, CartResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Item added to cart successfully")
                .result(response)
                .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String, CartResponse>> getCart(
            @RequestParam String cartToken) {
        
        CartResponse response = cartService.getCart(cartToken);
        
        return ResponseEntity.ok(
            ApiResponse.<String, CartResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Cart retrieved successfully")
                .result(response)
                .build()
        );
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<String, CartResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam String cartToken,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        CartResponse response = cartService.updateCartItem(cartToken, cartItemId, request);
        
        return ResponseEntity.ok(
            ApiResponse.<String, CartResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Cart item updated successfully")
                .result(response)
                .build()
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<String, CartResponse>> removeCartItem(
            @PathVariable Long cartItemId,
            @RequestParam String cartToken) {
        
        CartResponse response = cartService.removeCartItem(cartToken, cartItemId);
        
        return ResponseEntity.ok(
            ApiResponse.<String, CartResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Cart item removed successfully")
                .result(response)
                .build()
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String, Void>> clearCart(
            @RequestParam String cartToken) {
        
        cartService.clearCart(cartToken);
        
        return ResponseEntity.ok(
            ApiResponse.<String, Void>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Cart cleared successfully")
                .build()
        );
    }
}
