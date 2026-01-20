package wandererpi.lbs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import wandererpi.lbs.dto.request.CancelOrderRequest;
import wandererpi.lbs.dto.request.PlaceOrderRequest;
import wandererpi.lbs.dto.request.UpdateOrderStatusRequest;
import wandererpi.lbs.dto.response.OrderHistoryResponse;
import wandererpi.lbs.dto.response.OrderResponse;
import wandererpi.lbs.entity.*;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.enums.PaymentMethod;
import wandererpi.lbs.enums.ReservationStatus;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.jpa.*;
import wandererpi.lbs.service.impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SkuRepository skuRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Cart testCart;
    private Reservation testReservation;
    private Sku testSku;
    private Order testOrder;
    private OrderItem testOrderItem;
    private OrderHistory testOrderHistory;

    @BeforeEach
    void setUp() {
        // Setup test cart
        testCart = Cart.builder()
                .token("test-cart-token")
                .build();
        testCart.setId(1L);

        // Setup test category and product
        Category testCategory = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .build();
        testCategory.setId(1L);

        Product testProduct = Product.builder()
                .category(testCategory)
                .name("Test Product")
                .slug("test-product")
                .basePrice(new BigDecimal("500000"))
                .build();
        testProduct.setId(1L);

        // Setup test SKU
        testSku = Sku.builder()
                .product(testProduct)
                .skuCode("TEST-M-BLACK")
                .size("M")
                .color("Black")
                .price(new BigDecimal("500000"))
                .stockQty(10)
                .build();
        testSku.setId(1L);

        // Setup test reservation
        testReservation = Reservation.builder()
                .cart(testCart)
                .sku(testSku)
                .quantity(2)
                .status(ReservationStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(900)) // 15 minutes
                .build();
        testReservation.setId(1L);

        // Setup test order
        testOrder = Order.builder()
                .trackingToken("ABC123DEF456")
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .shippingAddr("Test Address")
                .paymentMethod(PaymentMethod.COD)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("1000000"))
                .build();
        testOrder.setId(1L);

        // Setup test order item
        testOrderItem = OrderItem.builder()
                .order(testOrder)
                .sku(testSku)
                .quantity(2)
                .unitPrice(new BigDecimal("500000"))
                .build();
        testOrderItem.setId(1L);

        // Setup test order history
        testOrderHistory = OrderHistory.builder()
                .order(testOrder)
                .oldStatus(null)
                .newStatus(OrderStatus.CONFIRMED.name())
                .note("Order created")
                .build();
        testOrderHistory.setId(1L);
    }

    @Test
    @DisplayName("Should place order successfully with COD payment")
    void shouldPlaceOrderSuccessfullyWithCODPayment() {
        // Given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .cartId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .shippingAddress("Test Address")
                .paymentMethod(PaymentMethod.COD)
                .build();

        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(testCart));
        when(reservationRepository.findByCartIdAndStatus(anyLong(), eq(ReservationStatus.ACTIVE)))
                .thenReturn(Collections.singletonList(testReservation));
        when(skuRepository.findById(anyLong())).thenReturn(Optional.of(testSku));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(testOrderHistory);

        // When
        OrderResponse response = orderService.placeOrder(request);

        // Then
        assertThat(response).isNotNull();

        verify(orderRepository).save(argThat(order -> 
                order.getStatus() == OrderStatus.CONFIRMED && 
                order.getPaymentMethod() == PaymentMethod.COD));
        verify(reservationRepository).save(argThat(reservation -> 
                reservation.getStatus() == ReservationStatus.CONSUMED));
        verify(cartItemRepository).deleteByCartId(testCart.getId());
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(emailService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    @DisplayName("Should place order with PENDING_PAYMENT status for Bank Transfer")
    void shouldPlaceOrderWithPendingPaymentStatusForBankTransfer() {
        // Given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .cartId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .shippingAddress("Test Address")
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(testCart));
        when(reservationRepository.findByCartIdAndStatus(anyLong(), eq(ReservationStatus.ACTIVE)))
                .thenReturn(Collections.singletonList(testReservation));
        when(skuRepository.findById(anyLong())).thenReturn(Optional.of(testSku));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(testOrderHistory);

        // When
        OrderResponse response = orderService.placeOrder(request);

        // Then
        assertThat(response).isNotNull();

        verify(orderRepository).save(argThat(order -> 
                order.getStatus() == OrderStatus.PENDING_PAYMENT && 
                order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER));
    }

    @Test
    @DisplayName("Should throw exception when cart not found")
    void shouldThrowExceptionWhenCartNotFoundOnPlaceOrder() {
        // Given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .cartId(999L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .shippingAddress("Test Address")
                .paymentMethod(PaymentMethod.COD)
                .build();

        when(cartRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when no active reservation found")
    void shouldThrowExceptionWhenNoActiveReservationFound() {
        // Given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .cartId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .shippingAddress("Test Address")
                .paymentMethod(PaymentMethod.COD)
                .build();

        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(testCart));
        when(reservationRepository.findByCartIdAndStatus(anyLong(), eq(ReservationStatus.ACTIVE)))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_ACTIVE_RESERVATION);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when reservation expired")
    void shouldThrowExceptionWhenReservationExpired() {
        // Given
        testReservation.setExpiresAt(Instant.now().minusSeconds(60)); // Expired

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .cartId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("081234567890")
                .shippingAddress("Test Address")
                .paymentMethod(PaymentMethod.COD)
                .build();

        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(testCart));
        when(reservationRepository.findByCartIdAndStatus(anyLong(), eq(ReservationStatus.ACTIVE)))
                .thenReturn(Collections.singletonList(testReservation));

        // When & Then
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_EXPIRED);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should get order by tracking token")
    void shouldGetOrderByTrackingToken() {
        // Given
        String trackingToken = "ABC123DEF456";

        when(orderRepository.findByTrackingToken(trackingToken)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Collections.singletonList(testOrderItem));

        // When
        OrderResponse response = orderService.getOrderByTrackingToken(trackingToken);

        // Then
        assertThat(response).isNotNull();

        verify(orderRepository).findByTrackingToken(trackingToken);
    }

    @Test
    @DisplayName("Should throw exception when order not found by tracking token")
    void shouldThrowExceptionWhenOrderNotFoundByTrackingToken() {
        // Given
        String trackingToken = "NONEXISTENT";

        when(orderRepository.findByTrackingToken(trackingToken)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderByTrackingToken(trackingToken))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void shouldUpdateOrderStatusSuccessfully() {
        // Given
        Long orderId = 1L;
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setNewStatus(OrderStatus.SHIPPING);
        request.setNote("Order dispatched");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(testOrderHistory);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Collections.singletonList(testOrderItem));

        // When
        OrderResponse response = orderService.updateOrderStatus(orderId, request, "admin@example.com");

        // Then
        assertThat(response).isNotNull();

        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.SHIPPING));
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(emailService).sendOrderStatusUpdate(any(Order.class), 
                eq(OrderStatus.CONFIRMED), eq(OrderStatus.SHIPPING));
    }

    @Test
    @DisplayName("Should throw exception for invalid status transition")
    void shouldThrowExceptionForInvalidStatusTransition() {
        // Given
        testOrder.setStatus(OrderStatus.DELIVERED); // Already delivered
        Long orderId = 1L;
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setNewStatus(OrderStatus.CANCELLED); // Cannot cancel delivered order

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, request, "admin@example.com"))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order and restore stock")
    void shouldCancelOrderAndRestoreStock() {
        // Given
        Long orderId = 1L;
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Customer requested cancellation");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Collections.singletonList(testOrderItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(testOrderHistory);

        // When
        OrderResponse response = orderService.cancelOrder(orderId, request, "admin@example.com");

        // Then
        assertThat(response).isNotNull();

        verify(jdbcTemplate).update(anyString(), anyInt(), anyLong());
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
        verify(orderHistoryRepository).save(any(OrderHistory.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling shipped order")
    void shouldThrowExceptionWhenCancellingShippedOrder() {
        // Given
        testOrder.setStatus(OrderStatus.SHIPPING);
        Long orderId = 1L;
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Customer requested cancellation");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, request, "admin@example.com"))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS);

        verify(jdbcTemplate, never()).update(anyString(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("Should get order history")
    void shouldGetOrderHistory() {
        // Given
        Long orderId = 1L;

        when(orderHistoryRepository.findByOrderIdOrderByUpdatedAtDesc(orderId))
                .thenReturn(Collections.singletonList(testOrderHistory));

        // When
        List<OrderHistoryResponse> response = orderService.getOrderHistory(orderId);

        // Then
        assertThat(response).isNotNull();

        verify(orderHistoryRepository).findByOrderIdOrderByUpdatedAtDesc(orderId);
    }
}
