package wandererpi.lbs.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.dto.request.AddToCartRequest;
import wandererpi.lbs.dto.request.PlaceOrderRequest;
import wandererpi.lbs.dto.request.StartCheckoutRequest;
import wandererpi.lbs.dto.response.CartResponse;
import wandererpi.lbs.dto.response.CheckoutSessionResponse;
import wandererpi.lbs.dto.response.OrderResponse;
import wandererpi.lbs.entity.Category;
import wandererpi.lbs.entity.Product;
import wandererpi.lbs.entity.Sku;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.enums.PaymentMethod;
import wandererpi.lbs.repository.CategoryRepository;
import wandererpi.lbs.repository.ProductRepository;
import wandererpi.lbs.repository.SkuRepository;
import wandererpi.lbs.service.CartService;
import wandererpi.lbs.service.OrderService;
import wandererpi.lbs.service.ReservationService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the complete checkout flow.
 * Tests the end-to-end customer journey from cart to order placement.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Checkout Flow Integration Tests")
class CheckoutFlowIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SkuRepository skuRepository;

    private Sku testSku;
    private String cartToken;

    @BeforeEach
    void setUp() {
        // Create test category
        Category category = Category.builder()
                .name("T-Shirts")
                .slug("t-shirts")
                .build();
        category = categoryRepository.save(category);

        // Create test product
        Product product = Product.builder()
                .category(category)
                .name("Test T-Shirt")
                .slug("test-t-shirt")
                .description("A test t-shirt for integration testing")
                .basePrice(new BigDecimal("500000"))
                .build();
        product = productRepository.save(product);

        // Create test SKU with available stock
        testSku = Sku.builder()
                .product(product)
                .skuCode("TEST-SHIRT-M-BLACK")
                .size("M")
                .color("Black")
                .price(new BigDecimal("500000"))
                .stockQty(10)
                .build();
        testSku = skuRepository.save(testSku);
    }

    @Test
    @DisplayName("Should complete full checkout flow with COD payment")
    void shouldCompleteFullCheckoutFlowWithCOD() {
        // Step 1: Add item to cart
        AddToCartRequest addToCartRequest = AddToCartRequest.builder()
                .cartToken(null) // Will generate new cart
                .skuId(testSku.getId())
                .quantity(2)
                .build();

        CartResponse cartResponse = cartService.addToCart(addToCartRequest);
        assertThat(cartResponse).isNotNull();
        assertThat(cartResponse.getCartToken()).isNotNull();
        assertThat(cartResponse.getItems()).hasSize(1);
        assertThat(cartResponse.getItems().get(0).getQuantity()).isEqualTo(2);

        cartToken = cartResponse.getCartToken();

        // Step 2: Start checkout (create reservation)
        StartCheckoutRequest startCheckoutRequest = StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(cartResponse.getItems().stream()
                        .map(item -> StartCheckoutRequest.CheckoutItem.builder()
                                .cartItemId(item.getSkuId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
        CheckoutSessionResponse checkoutResponse = reservationService.startCheckout(startCheckoutRequest);
        assertThat(checkoutResponse).isNotNull();
        assertThat(checkoutResponse.getCartId()).isNotNull();
        assertThat(checkoutResponse.getReservations()).hasSize(1);
        assertThat(checkoutResponse.getReservations().get(0).getQuantity()).isEqualTo(2);

        // Step 3: Place order
        PlaceOrderRequest placeOrderRequest = PlaceOrderRequest.builder()
                .cartId(checkoutResponse.getCartId())
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .customerPhone("081234567890")
                .shippingAddress("Jl. Test No. 123, Jakarta")
                .paymentMethod(PaymentMethod.COD)
                .build();

        OrderResponse orderResponse = orderService.placeOrder(placeOrderRequest);
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getTrackingToken()).isNotNull();
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(orderResponse.getPaymentMethod()).isEqualTo(PaymentMethod.COD);
        assertThat(orderResponse.getCustomerName()).isEqualTo("John Doe");
        assertThat(orderResponse.getCustomerEmail()).isEqualTo("john.doe@example.com");

        // Step 4: Verify cart is cleared
        CartResponse clearedCart = cartService.getCart(cartToken);
        assertThat(clearedCart.getItems()).isEmpty();

        // Step 5: Verify stock was reduced
        Sku updatedSku = skuRepository.findById(testSku.getId()).orElseThrow();
        assertThat(updatedSku.getStockQty()).isEqualTo(8); // 10 - 2 = 8

        // Step 6: Verify order can be tracked
        OrderResponse trackedOrder = orderService.getOrderByTrackingToken(orderResponse.getTrackingToken());
        assertThat(trackedOrder).isNotNull();
        assertThat(trackedOrder.getTrackingToken()).isEqualTo(orderResponse.getTrackingToken());
    }

    @Test
    @DisplayName("Should complete full checkout flow with Bank Transfer payment")
    void shouldCompleteFullCheckoutFlowWithBankTransfer() {
        // Step 1: Add item to cart
        AddToCartRequest addToCartRequest = AddToCartRequest.builder()
                .cartToken(null)
                .skuId(testSku.getId())
                .quantity(3)
                .build();

        CartResponse cartResponse = cartService.addToCart(addToCartRequest);
        cartToken = cartResponse.getCartToken();

        // Step 2: Start checkout
        StartCheckoutRequest startCheckoutRequest = StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(cartResponse.getItems().stream()
                        .map(item -> StartCheckoutRequest.CheckoutItem.builder()
                                .cartItemId(item.getSkuId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
        CheckoutSessionResponse checkoutResponse = reservationService.startCheckout(startCheckoutRequest);
        assertThat(checkoutResponse).isNotNull();

        // Step 3: Place order with Bank Transfer
        PlaceOrderRequest placeOrderRequest = PlaceOrderRequest.builder()
                .cartId(checkoutResponse.getCartId())
                .customerName("Jane Smith")
                .customerEmail("jane.smith@example.com")
                .customerPhone("082345678901")
                .shippingAddress("Jl. Bank No. 456, Bandung")
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        OrderResponse orderResponse = orderService.placeOrder(placeOrderRequest);
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(orderResponse.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);

        // Verify stock was still reduced (reserved for pending payment)
        Sku updatedSku = skuRepository.findById(testSku.getId()).orElseThrow();
        assertThat(updatedSku.getStockQty()).isEqualTo(7); // 10 - 3 = 7
    }

    @Test
    @DisplayName("Should handle multiple items in cart")
    void shouldHandleMultipleItemsInCart() {
        // Create another SKU
        Sku testSku2 = Sku.builder()
                .product(testSku.getProduct())
                .skuCode("TEST-SHIRT-L-WHITE")
                .size("L")
                .color("White")
                .price(new BigDecimal("550000"))
                .stockQty(15)
                .build();
        testSku2 = skuRepository.save(testSku2);

        // Add first item to cart
        AddToCartRequest request1 = AddToCartRequest.builder()
                .cartToken(null)
                .skuId(testSku.getId())
                .quantity(2)
                .build();
        CartResponse cartResponse = cartService.addToCart(request1);
        cartToken = cartResponse.getCartToken();

        // Add second item to cart
        AddToCartRequest request2 = AddToCartRequest.builder()
                .cartToken(cartToken)
                .skuId(testSku2.getId())
                .quantity(3)
                .build();
        cartResponse = cartService.addToCart(request2);
        
        assertThat(cartResponse.getItems()).hasSize(2);

        // Start checkout
        StartCheckoutRequest startCheckoutRequest = StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(cartResponse.getItems().stream()
                        .map(item -> StartCheckoutRequest.CheckoutItem.builder()
                                .cartItemId(item.getSkuId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
        CheckoutSessionResponse checkoutResponse = reservationService.startCheckout(startCheckoutRequest);
        assertThat(checkoutResponse.getReservations()).hasSize(2);

        // Place order
        PlaceOrderRequest placeOrderRequest = PlaceOrderRequest.builder()
                .cartId(checkoutResponse.getCartId())
                .customerName("Multi Item Customer")
                .customerEmail("multi@example.com")
                .customerPhone("083456789012")
                .shippingAddress("Jl. Multi No. 789, Surabaya")
                .paymentMethod(PaymentMethod.COD)
                .build();

        OrderResponse orderResponse = orderService.placeOrder(placeOrderRequest);
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // Verify both SKUs had stock reduced
        Sku updatedSku1 = skuRepository.findById(testSku.getId()).orElseThrow();
        Sku updatedSku2 = skuRepository.findById(testSku2.getId()).orElseThrow();
        
        assertThat(updatedSku1.getStockQty()).isEqualTo(8); // 10 - 2
        assertThat(updatedSku2.getStockQty()).isEqualTo(12); // 15 - 3
    }

    @Test
    @DisplayName("Should adjust cart quantity when exceeding stock during add to cart")
    void shouldAdjustQuantityWhenExceedingStock() {
        // Try to add more than available stock
        AddToCartRequest request = AddToCartRequest.builder()
                .cartToken(null)
                .skuId(testSku.getId())
                .quantity(15) // More than available stock (10)
                .build();

        CartResponse cartResponse = cartService.addToCart(request);
        
        assertThat(cartResponse.getItems()).hasSize(1);
        assertThat(cartResponse.getItems().get(0).getQuantity()).isEqualTo(10); // Adjusted to available stock
        assertThat(cartResponse.getWarnings()).isNotEmpty();
        assertThat(cartResponse.getWarnings().get(0).getMessage()).contains("adjusted");
    }

    @Test
    @DisplayName("Should preserve cart token across operations")
    void shouldPreserveCartTokenAcrossOperations() {
        // Create cart
        AddToCartRequest request = AddToCartRequest.builder()
                .cartToken(null)
                .skuId(testSku.getId())
                .quantity(1)
                .build();

        CartResponse cartResponse1 = cartService.addToCart(request);
        String initialToken = cartResponse1.getCartToken();

        // Get cart by token
        CartResponse cartResponse2 = cartService.getCart(initialToken);
        assertThat(cartResponse2.getCartToken()).isEqualTo(initialToken);

        // Add another item with same token
        AddToCartRequest request2 = AddToCartRequest.builder()
                .cartToken(initialToken)
                .skuId(testSku.getId())
                .quantity(2)
                .build();

        CartResponse cartResponse3 = cartService.addToCart(request2);
        assertThat(cartResponse3.getCartToken()).isEqualTo(initialToken);
        assertThat(cartResponse3.getItems().get(0).getQuantity()).isEqualTo(3); // 1 + 2
    }
}
