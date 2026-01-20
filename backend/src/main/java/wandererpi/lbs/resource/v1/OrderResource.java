package wandererpi.lbs.resource.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import wandererpi.lbs.dto.request.CancelOrderRequest;
import wandererpi.lbs.dto.request.PlaceOrderRequest;
import wandererpi.lbs.dto.request.UpdateOrderStatusRequest;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.dto.response.OrderHistoryResponse;
import wandererpi.lbs.dto.response.OrderResponse;
import wandererpi.lbs.dto.response.OrderSummaryResponse;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.service.OrderService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderResource {
    
    private final OrderService orderService;

    /**
     * Place order - Public endpoint (no authentication required)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String, OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        
        OrderResponse response = orderService.placeOrder(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<String, OrderResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.CREATED.value())
                .message("Order placed successfully")
                .result(response)
                .build()
        );
    }

    /**
     * Track order by tracking token - Public endpoint (no authentication required)
     */
    @GetMapping("/track/{trackingToken}")
    public ResponseEntity<ApiResponse<String, OrderResponse>> trackOrder(
            @PathVariable String trackingToken) {
        
        OrderResponse response = orderService.getOrderByTrackingToken(trackingToken);
        
        return ResponseEntity.ok(
            ApiResponse.<String, OrderResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Order retrieved successfully")
                .result(response)
                .build()
        );
    }

    /**
     * Get order by ID - Admin only
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String, OrderResponse>> getOrderById(
            @PathVariable Long orderId) {
        
        OrderResponse response = orderService.getOrderById(orderId);
        
        return ResponseEntity.ok(
            ApiResponse.<String, OrderResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Order retrieved successfully")
                .result(response)
                .build()
        );
    }

    /**
     * Get paginated orders - Admin only
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String, Page<OrderSummaryResponse>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Page<OrderSummaryResponse> response = orderService.getOrders(status, startDate, endDate, page, size);
        
        return ResponseEntity.ok(
            ApiResponse.<String, Page<OrderSummaryResponse>>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Orders retrieved successfully")
                .result(response)
                .build()
        );
    }

    /**
     * Update order status - Admin only
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String, OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String adminEmail = userDetails.getUsername();
        OrderResponse response = orderService.updateOrderStatus(orderId, request, adminEmail);
        
        return ResponseEntity.ok(
            ApiResponse.<String, OrderResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Order status updated successfully")
                .result(response)
                .build()
        );
    }

    /**
     * Cancel order - Admin only
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String, OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String adminEmail = userDetails.getUsername();
        OrderResponse response = orderService.cancelOrder(orderId, request, adminEmail);
        
        return ResponseEntity.ok(
            ApiResponse.<String, OrderResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Order cancelled successfully")
                .result(response)
                .build()
        );
    }

    /**
     * Get order history - Admin only
     */
    @GetMapping("/{orderId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String, List<OrderHistoryResponse>>> getOrderHistory(
            @PathVariable Long orderId) {
        
        List<OrderHistoryResponse> response = orderService.getOrderHistory(orderId);
        
        return ResponseEntity.ok(
            ApiResponse.<String, List<OrderHistoryResponse>>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Order history retrieved successfully")
                .result(response)
                .build()
        );
    }
}
