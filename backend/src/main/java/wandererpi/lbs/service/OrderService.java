package wandererpi.lbs.service;

import org.springframework.data.domain.Page;
import wandererpi.lbs.dto.request.CancelOrderRequest;
import wandererpi.lbs.dto.request.PlaceOrderRequest;
import wandererpi.lbs.dto.request.UpdateOrderStatusRequest;
import wandererpi.lbs.dto.response.OrderHistoryResponse;
import wandererpi.lbs.dto.response.OrderResponse;
import wandererpi.lbs.dto.response.OrderSummaryResponse;
import wandererpi.lbs.enums.OrderStatus;

import java.time.Instant;
import java.util.List;

public interface OrderService {
    
    /**
     * Place order from active reservations
     */
    OrderResponse placeOrder(PlaceOrderRequest request);
    
    /**
     * Get order by tracking token (for customer tracking)
     */
    OrderResponse getOrderByTrackingToken(String trackingToken);
    
    /**
     * Get order by ID (for admin)
     */
    OrderResponse getOrderById(Long orderId);
    
    /**
     * Get paginated orders (for admin)
     */
    Page<OrderSummaryResponse> getOrders(OrderStatus status, Instant startDate, 
                                          Instant endDate, Integer page, Integer size);
    
    /**
     * Update order status (for admin)
     */
    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String adminEmail);
    
    /**
     * Cancel order and restore stock (for admin)
     */
    OrderResponse cancelOrder(Long orderId, CancelOrderRequest request, String adminEmail);
    
    /**
     * Get order history (for admin)
     */
    List<OrderHistoryResponse> getOrderHistory(Long orderId);
}
