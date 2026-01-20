package wandererpi.lbs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import wandererpi.lbs.dto.request.StartCheckoutRequest;
import wandererpi.lbs.dto.response.CheckoutSessionResponse;
import wandererpi.lbs.entity.*;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.enums.ReservationStatus;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.jpa.CartItemRepository;
import wandererpi.lbs.repository.jpa.CartRepository;
import wandererpi.lbs.repository.jpa.ReservationRepository;
import wandererpi.lbs.service.impl.ReservationServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Cart testCart;
    private CartItem testCartItem;
    private Sku testSku;
    private Product testProduct;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        // Setup test cart
        testCart = Cart.builder()
                .token("test-cart-token")
                .build();
        testCart.setId(1L);

        // Setup test category
        Category testCategory = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .build();
        testCategory.setId(1L);

        // Setup test product
        testProduct = Product.builder()
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

        // Setup test cart item
        testCartItem = CartItem.builder()
                .cart(testCart)
                .sku(testSku)
                .quantity(2)
                .build();
        testCartItem.setId(1L);

        // Setup test reservation
        testReservation = Reservation.builder()
                .cart(testCart)
                .sku(testSku)
                .quantity(2)
                .status(ReservationStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(900)) // 15 minutes
                .build();
        testReservation.setId(1L);
    }

    @Test
    @DisplayName("Should start checkout successfully with available stock")
    void shouldStartCheckoutSuccessfullyWithAvailableStock() {
        // Given
        String cartToken = "test-cart-token";
        StartCheckoutRequest request = StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(List.of(
                        StartCheckoutRequest.CheckoutItem.builder()
                                .cartItemId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();

        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(testCartItem));
        when(jdbcTemplate.update(anyString(), anyInt(), anyLong(), anyInt())).thenReturn(1);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When
        CheckoutSessionResponse response = reservationService.startCheckout(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(testCart.getId());

        verify(cartRepository).findByToken(cartToken);
        verify(jdbcTemplate).update(anyString(), anyInt(), anyLong(), anyInt());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw exception when cart is empty")
    void shouldThrowExceptionWhenCartIsEmpty() {
        // Given
        String cartToken = "test-cart-token";
        StartCheckoutRequest request = StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(Collections.emptyList())
                .build();

        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.of(testCart));

        // When & Then
        assertThatThrownBy(() -> reservationService.startCheckout(request))
                .isInstanceOf(ApplicationException.class);

        verify(cartRepository).findByToken(cartToken);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw exception when cart not found")
    void shouldThrowExceptionWhenCartNotFound() {
        // Given
        String cartToken = "nonexistent-token";
        StartCheckoutRequest request = StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(List.of())
                .build();

        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.startCheckout(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);

        verify(cartRepository).findByToken(cartToken);
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given
        String cartToken = "test-cart-token";
        testSku.setStockQty(1); // Less than requested quantity
        testCartItem.setQuantity(5);

        StartCheckoutRequest request = StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(List.of(
                        StartCheckoutRequest.CheckoutItem.builder()
                                .cartItemId(1L)
                                .quantity(5)
                                .build()
                ))
                .build();

        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(testCartItem));
        when(jdbcTemplate.update(anyString(), anyInt(), anyLong(), anyInt())).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> reservationService.startCheckout(request))
                .isInstanceOf(ApplicationException.class);

        verify(jdbcTemplate).update(anyString(), anyInt(), anyLong(), anyInt());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should start checkout with selected items only")
    void shouldStartCheckoutWithSelectedItemsOnly() {
        // Given
        StartCheckoutRequest request = StartCheckoutRequest.builder()
                .cartToken("test-cart-token")
                .items(List.of(
                        StartCheckoutRequest.CheckoutItem.builder()
                                .cartItemId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();

        when(cartRepository.findByToken(anyString())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(testCartItem));
        when(jdbcTemplate.update(anyString(), anyInt(), anyLong(), anyInt())).thenReturn(1);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When
        CheckoutSessionResponse response = reservationService.startCheckout(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(testCart.getId());

        verify(cartItemRepository).findAllById(anyList());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should validate active reservation successfully")
    void shouldValidateActiveReservationSuccessfully() {
        // Given
        Long cartId = 1L;

        when(reservationRepository.findByCartIdAndStatus(cartId, ReservationStatus.ACTIVE))
                .thenReturn(Collections.singletonList(testReservation));

        // When & Then
        assertThatCode(() -> reservationService.validateReservation(cartId))
                .doesNotThrowAnyException();

        verify(reservationRepository).findByCartIdAndStatus(cartId, ReservationStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should invalidate expired reservation")
    void shouldInvalidateExpiredReservation() {
        // Given
        Long cartId = 1L;
        testReservation.setExpiresAt(Instant.now().minusSeconds(60)); // Expired

        when(reservationRepository.findByCartIdAndStatus(cartId, ReservationStatus.ACTIVE))
                .thenReturn(Collections.singletonList(testReservation));

        // When & Then
        assertThatThrownBy(() -> reservationService.validateReservation(cartId))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Should throw exception when no reservation found")
    void shouldReturnFalseWhenNoReservationFound() {
        // Given
        Long cartId = 1L;

        when(reservationRepository.findByCartIdAndStatus(cartId, ReservationStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> reservationService.validateReservation(cartId))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Should release expired reservations")
    void shouldReleaseExpiredReservations() {
        // Given
        testReservation.setExpiresAt(Instant.now().minusSeconds(60));

        when(reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.ACTIVE), any(Instant.class)))
                .thenReturn(Collections.singletonList(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When
        int releasedCount = reservationService.releaseExpiredReservations();

        // Then
        assertThat(releasedCount).isEqualTo(1);

        verify(reservationRepository).findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.ACTIVE), any(Instant.class));
        verify(jdbcTemplate).update(anyString(), anyInt(), anyLong());
        verify(reservationRepository).save(argThat(reservation -> 
                reservation.getStatus() == ReservationStatus.EXPIRED));
    }

    @Test
    @DisplayName("Should return zero when no expired reservations found")
    void shouldReturnZeroWhenNoExpiredReservationsFound() {
        // Given
        when(reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.ACTIVE), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // When
        int releasedCount = reservationService.releaseExpiredReservations();

        // Then
        assertThat(releasedCount).isEqualTo(0);

        verify(reservationRepository).findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.ACTIVE), any(Instant.class));
        verify(jdbcTemplate, never()).update(anyString(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("Should handle reservation cleanup errors gracefully")
    void shouldHandleReservationCleanupErrorsGracefully() {
        // Given
        testReservation.setExpiresAt(Instant.now().minusSeconds(60));

        when(reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.ACTIVE), any(Instant.class)))
                .thenReturn(Collections.singletonList(testReservation));
        when(jdbcTemplate.update(anyString(), anyInt(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        int releasedCount = reservationService.releaseExpiredReservations();

        // Then
        assertThat(releasedCount).isEqualTo(0);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
