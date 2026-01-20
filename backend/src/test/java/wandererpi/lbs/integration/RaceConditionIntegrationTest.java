package wandererpi.lbs.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.dto.request.AddToCartRequest;
import wandererpi.lbs.dto.response.CartResponse;
import wandererpi.lbs.dto.response.CheckoutSessionResponse;
import wandererpi.lbs.dto.request.StartCheckoutRequest;
import wandererpi.lbs.entity.Category;
import wandererpi.lbs.entity.Product;
import wandererpi.lbs.entity.Sku;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.CategoryRepository;
import wandererpi.lbs.repository.ProductRepository;
import wandererpi.lbs.repository.SkuRepository;
import wandererpi.lbs.service.CartService;
import wandererpi.lbs.service.ReservationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Critical integration test for race condition scenarios.
 * <p>
 * Tests the most important business requirement: preventing overselling
 * when multiple customers try to purchase the last available item(s) simultaneously.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Race Condition Integration Tests")
class RaceConditionIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SkuRepository skuRepository;

    private Sku testSku;

    @BeforeEach
    void setUp() {
        // Create test category
        Category category = Category.builder()
                .name("Limited Edition")
                .slug("limited-edition")
                .build();
        category = categoryRepository.save(category);

        // Create test product
        Product product = Product.builder()
                .category(category)
                .name("Limited T-Shirt")
                .slug("limited-t-shirt")
                .description("A limited edition t-shirt - only 1 left!")
                .basePrice(new BigDecimal("1000000"))
                .build();
        product = productRepository.save(product);

        // Create test SKU with ONLY 1 ITEM in stock
        testSku = Sku.builder()
                .product(product)
                .skuCode("LIMITED-SHIRT-M-BLACK")
                .size("M")
                .color("Black")
                .price(new BigDecimal("1000000"))
                .stockQty(1) // CRITICAL: Only 1 item available
                .build();
        testSku = skuRepository.save(testSku);
    }

    @Test
    @DisplayName("CRITICAL: Two customers trying to buy the last item simultaneously - only one should succeed")
    @Transactional
    void testConcurrentReservationOfLastItem() throws Exception {
        // Create 2 separate carts, each with the same last item
        String cartToken1 = createCartWithItem(testSku.getId(), 1);
        String cartToken2 = createCartWithItem(testSku.getId(), 1);

        // Execute checkouts concurrently using a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        List<Future<CheckoutResult>> futures = new ArrayList<>();
        
        // Customer 1 attempts checkout
        futures.add(executor.submit(() -> {
            try {
                CheckoutSessionResponse response = reservationService.startCheckout(
                        createStartCheckoutRequest(cartToken1));
                return new CheckoutResult(true, response, null);
            } catch (ApplicationException e) {
                return new CheckoutResult(false, null, e);
            } catch (Exception e) {
                return new CheckoutResult(false, null, e);
            }
        }));
        
        // Customer 2 attempts checkout (at the same time)
        futures.add(executor.submit(() -> {
            try {
                CheckoutSessionResponse response = reservationService.startCheckout(
                        createStartCheckoutRequest(cartToken2));
                return new CheckoutResult(true, response, null);
            } catch (Exception e) {
                return new CheckoutResult(false, null, e);
            }
        }));
        
        // Wait for both attempts to complete
        List<CheckoutResult> results = new ArrayList<>();
        for (Future<CheckoutResult> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Count successes and failures
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        // CRITICAL ASSERTION: Exactly one checkout should succeed
        assertThat(successCount).as("Exactly one checkout should succeed")
                .isEqualTo(1);
        assertThat(failureCount).as("Exactly one checkout should fail")
                .isEqualTo(1);
        
        // CRITICAL ASSERTION: Final stock should be 0, NOT negative
        Sku finalSku = skuRepository.findById(testSku.getId()).orElseThrow();
        assertThat(finalSku.getStockQty())
                .as("Stock should be exactly 0, not negative")
                .isEqualTo(0);
        
        // Verify the failure was due to insufficient stock
        CheckoutResult failedResult = results.stream()
                .filter(r -> !r.success)
                .findFirst()
                .orElseThrow();
        
        assertThat(failedResult.exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("CRITICAL: Three customers trying to buy last 2 items - only two should succeed")
    @Transactional
    void testConcurrentReservationWith2ItemsAnd3Customers() throws Exception {
        // Update SKU to have 2 items
        testSku.setStockQty(2);
        testSku = skuRepository.save(testSku);

        // Create 3 separate carts, each trying to buy 1 item
        String cartToken1 = createCartWithItem(testSku.getId(), 1);
        String cartToken2 = createCartWithItem(testSku.getId(), 1);
        String cartToken3 = createCartWithItem(testSku.getId(), 1);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        List<Future<CheckoutResult>> futures = new ArrayList<>();
        futures.add(executor.submit(() -> attemptCheckout(cartToken1)));
        futures.add(executor.submit(() -> attemptCheckout(cartToken2)));
        futures.add(executor.submit(() -> attemptCheckout(cartToken3)));
        
        List<CheckoutResult> results = new ArrayList<>();
        for (Future<CheckoutResult> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        // Exactly 2 should succeed (we have 2 items)
        assertThat(successCount).as("Exactly two checkouts should succeed")
                .isEqualTo(2);
        assertThat(failureCount).as("Exactly one checkout should fail")
                .isEqualTo(1);
        
        // Final stock should be 0
        Sku finalSku = skuRepository.findById(testSku.getId()).orElseThrow();
        assertThat(finalSku.getStockQty())
                .as("Stock should be exactly 0")
                .isEqualTo(0);
    }

    @Test
    @DisplayName("CRITICAL: Multiple customers trying to buy different quantities of last items")
    @Transactional
    void testConcurrentReservationWithDifferentQuantities() throws Exception {
        // Update SKU to have 5 items
        testSku.setStockQty(5);
        testSku = skuRepository.save(testSku);

        // Customer 1 wants 3 items
        String cartToken1 = createCartWithItem(testSku.getId(), 3);
        // Customer 2 wants 4 items (will fail because only 5 total, customer 1 might take 3)
        String cartToken2 = createCartWithItem(testSku.getId(), 4);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        List<Future<CheckoutResult>> futures = new ArrayList<>();
        futures.add(executor.submit(() -> attemptCheckout(cartToken1)));
        futures.add(executor.submit(() -> attemptCheckout(cartToken2)));
        
        List<CheckoutResult> results = new ArrayList<>();
        for (Future<CheckoutResult> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        // Only one can succeed (they can't both fit in 5 items)
        assertThat(successCount).as("Exactly one checkout should succeed")
                .isEqualTo(1);
        assertThat(failureCount).as("Exactly one checkout should fail")
                .isEqualTo(1);
        
        // Verify final stock is non-negative
        Sku finalSku = skuRepository.findById(testSku.getId()).orElseThrow();
        assertThat(finalSku.getStockQty())
                .as("Stock should not be negative")
                .isGreaterThanOrEqualTo(0);
        
        // Final stock should be either 2 (if customer 1 succeeded) or 1 (if customer 2 succeeded)
        assertThat(finalSku.getStockQty())
                .as("Stock should be either 2 or 1")
                .isIn(1, 2);
    }

    @Test
    @DisplayName("High concurrency stress test - 10 customers, 5 items")
    @Transactional
    void testHighConcurrencyStressTest() throws Exception {
        // Update SKU to have 5 items
        testSku.setStockQty(5);
        testSku = skuRepository.save(testSku);

        // Create 10 customers, each wanting 1 item
        int customerCount = 10;
        List<String> cartTokens = new ArrayList<>();
        for (int i = 0; i < customerCount; i++) {
            cartTokens.add(createCartWithItem(testSku.getId(), 1));
        }

        ExecutorService executor = Executors.newFixedThreadPool(customerCount);
        
        List<Future<CheckoutResult>> futures = new ArrayList<>();
        for (String cartToken : cartTokens) {
            futures.add(executor.submit(() -> attemptCheckout(cartToken)));
        }
        
        List<CheckoutResult> results = new ArrayList<>();
        for (Future<CheckoutResult> future : futures) {
            results.add(future.get(15, TimeUnit.SECONDS));
        }
        
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);
        
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        // Exactly 5 should succeed (we have 5 items)
        assertThat(successCount).as("Exactly 5 checkouts should succeed")
                .isEqualTo(5);
        assertThat(failureCount).as("Exactly 5 checkouts should fail")
                .isEqualTo(5);
        
        // CRITICAL: Final stock must be exactly 0, not negative
        Sku finalSku = skuRepository.findById(testSku.getId()).orElseThrow();
        assertThat(finalSku.getStockQty())
                .as("Stock must be exactly 0 after all successful reservations")
                .isEqualTo(0);
    }

    // Helper methods

    private String createCartWithItem(Long skuId, int quantity) {
        AddToCartRequest request = AddToCartRequest.builder()
                .cartToken(null)
                .skuId(skuId)
                .quantity(quantity)
                .build();
        
        CartResponse cartResponse = cartService.addToCart(request);
        return cartResponse.getCartToken();
    }

    private CheckoutResult attemptCheckout(String cartToken) {
        try {
            CheckoutSessionResponse response = reservationService.startCheckout(
                    createStartCheckoutRequest(cartToken));
            return new CheckoutResult(true, response, null);
        } catch (ApplicationException e) {
            return new CheckoutResult(false, null, e);
        } catch (Exception e) {
            return new CheckoutResult(false, null, e);
        }
    }

    private StartCheckoutRequest createStartCheckoutRequest(String cartToken) {
        CartResponse cart = cartService.getCart(cartToken);
        return StartCheckoutRequest.builder()
                .cartToken(cartToken)
                .items(cart.getItems().stream()
                        .map(item -> StartCheckoutRequest.CheckoutItem.builder()
                                .cartItemId(item.getSkuId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }

    /**
     * Helper class to capture checkout attempt results
     */
    private static class CheckoutResult {
        final boolean success;
        final CheckoutSessionResponse response;
        final Exception exception;

        CheckoutResult(boolean success, CheckoutSessionResponse response, Exception exception) {
            this.success = success;
            this.response = response;
            this.exception = exception;
        }
    }
}
