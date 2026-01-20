package wandererpi.lbs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wandererpi.lbs.dto.request.AddToCartRequest;
import wandererpi.lbs.dto.request.UpdateCartItemRequest;
import wandererpi.lbs.dto.response.CartResponse;
import wandererpi.lbs.entity.*;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.CartItemRepository;
import wandererpi.lbs.repository.CartRepository;
import wandererpi.lbs.repository.SkuRepository;
import wandererpi.lbs.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private SkuRepository skuRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private Sku testSku;
    private CartItem testCartItem;
    private Product testProduct;

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
    }

    @Test
    @DisplayName("Should create new cart when token is null")
    void shouldCreateNewCartWhenTokenIsNull() {
        // Given
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(1L);
            return cart;
        });

        // When
        CartResponse response = cartService.getOrCreateCart(null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCartToken()).isNotNull();
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should get existing cart by token")
    void shouldGetExistingCartByToken() {
        // Given
        String cartToken = "existing-cart-token";
        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(new ArrayList<>());

        // When
        CartResponse response = cartService.getOrCreateCart(cartToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(testCart.getId());
        assertThat(response.getCartToken()).isEqualTo(testCart.getToken());

        verify(cartRepository).findByToken(cartToken);
    }

    @Test
    @DisplayName("Should add item to cart successfully")
    void shouldAddItemToCartSuccessfully() {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .cartToken("test-cart-token")
                .skuId(1L)
                .quantity(2)
                .build();

        when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
        when(cartRepository.findByToken(anyString())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndSkuId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Collections.singletonList(testCartItem));

        // When
        CartResponse response = cartService.addToCart(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCartToken()).isEqualTo(testCart.getToken());

        verify(skuRepository).findById(1L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should throw exception when SKU not found")
    void shouldThrowExceptionWhenSkuNotFound() {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .cartToken("test-cart-token")
                .skuId(999L)
                .quantity(2)
                .build();

        when(skuRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.addToCart(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SKU_NOT_FOUND);

        verify(skuRepository).findById(999L);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should adjust quantity when adding more than available stock")
    void shouldAdjustQuantityWhenAddingMoreThanAvailableStock() {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .cartToken("test-cart-token")
                .skuId(1L)
                .quantity(15) // More than available stock (10)
                .build();

        when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
        when(cartRepository.findByToken(anyString())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndSkuId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Collections.singletonList(testCartItem));

        // When
        CartResponse response = cartService.addToCart(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getWarnings()).isNotEmpty();

        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 10));
    }

    @Test
    @DisplayName("Should update cart item quantity")
    void shouldUpdateCartItemQuantity() {
        // Given
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        when(cartRepository.findByToken(anyString())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));
        when(skuRepository.findById(anyLong())).thenReturn(Optional.of(testSku));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Collections.singletonList(testCartItem));

        // When
        CartResponse response = cartService.updateCartItem("test-cart-token", 1L, request);

        // Then
        assertThat(response).isNotNull();

        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should remove cart item when quantity is zero")
    void shouldRemoveCartItemWhenQuantityIsZero() {
        // Given
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(0)
                .build();

        when(cartRepository.findByToken(anyString())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(new ArrayList<>());

        // When
        CartResponse response = cartService.updateCartItem("test-cart-token", 1L, request);

        // Then
        assertThat(response).isNotNull();

        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    @DisplayName("Should throw exception when cart item not found")
    void shouldThrowExceptionWhenCartItemNotFound() {
        // Given
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        when(cartRepository.findByToken(anyString())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.updateCartItem("test-cart-token", 999L, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_FOUND);

        verify(cartItemRepository).findById(999L);
    }

    @Test
    @DisplayName("Should remove cart item successfully")
    void shouldRemoveCartItemSuccessfully() {
        // Given
        Long cartItemId = 1L;

        when(cartRepository.findByToken(anyString())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(new ArrayList<>());

        // When
        CartResponse response = cartService.removeCartItem("test-cart-token", cartItemId);

        // Then
        assertThat(response).isNotNull();

        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    @DisplayName("Should get cart by token")
    void shouldGetCartByToken() {
        // Given
        String cartToken = "test-cart-token";

        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(new ArrayList<>());

        // When
        CartResponse response = cartService.getCart(cartToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCartToken()).isEqualTo(cartToken);

        verify(cartRepository).findByToken(cartToken);
    }

    @Test
    @DisplayName("Should throw exception when cart not found")
    void shouldThrowExceptionWhenCartNotFound() {
        // Given
        String cartToken = "nonexistent-token";

        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.getCart(cartToken))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_NOT_FOUND);

        verify(cartRepository).findByToken(cartToken);
    }

    @Test
    @DisplayName("Should clear cart successfully")
    void shouldClearCartSuccessfully() {
        // Given
        String cartToken = "test-cart-token";

        when(cartRepository.findByToken(cartToken)).thenReturn(Optional.of(testCart));

        // When
        cartService.clearCart(cartToken);

        // Then
        verify(cartItemRepository).deleteByCartId(testCart.getId());
    }
}
