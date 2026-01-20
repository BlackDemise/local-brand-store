package wandererpi.lbs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.dto.response.*;
import wandererpi.lbs.entity.Category;
import wandererpi.lbs.entity.Product;
import wandererpi.lbs.entity.ProductImage;
import wandererpi.lbs.entity.Sku;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.jpa.CategoryRepository;
import wandererpi.lbs.repository.jpa.ProductImageRepository;
import wandererpi.lbs.repository.jpa.ProductRepository;
import wandererpi.lbs.repository.jpa.SkuRepository;
import wandererpi.lbs.service.impl.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SkuRepository skuRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private Sku testSku;
    private ProductImage testImage;

    @BeforeEach
    void setUp() {
        // Setup test category
        testCategory = Category.builder()
                .name("T-Shirts")
                .slug("t-shirts")
                .build();
        testCategory.setId(1L);

        // Setup test product
        testProduct = Product.builder()
                .category(testCategory)
                .name("Test Product")
                .slug("test-product")
                .description("Test description")
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

        // Setup test image
        testImage = ProductImage.builder()
                .product(testProduct)
                .imageUrl("https://example.com/image.jpg")
                .position(1)
                .isPrimary(true)
                .build();
        testImage.setId(1L);
    }

    @Test
    @DisplayName("Should get products with pagination")
    void shouldGetProductsWithPagination() {
        // Given
        ProductFilterRequest request = ProductFilterRequest.builder()
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        List<Product> products = Collections.singletonList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        List<ProductImage> images = Collections.singletonList(testImage);
        when(productImageRepository.findByProductId(anyLong())).thenReturn(images);
        when(skuRepository.findByProductId(anyLong())).thenReturn(Collections.singletonList(testSku));

        // When
        PageResponse<ProductListResponse> response = productService.getProducts(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(1);

        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter products by category")
    void shouldFilterProductsByCategory() {
        // Given
        ProductFilterRequest request = ProductFilterRequest.builder()
                .categoryId(1L)
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        List<Product> products = Collections.singletonList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productRepository.findByCategoryId(anyLong(), any(Pageable.class)))
                .thenReturn(productPage);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        List<ProductImage> images = Collections.singletonList(testImage);
        when(productImageRepository.findByProductId(anyLong())).thenReturn(images);
        when(skuRepository.findByProductId(anyLong())).thenReturn(Collections.singletonList(testSku));

        // When
        PageResponse<ProductListResponse> response = productService.getProducts(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(productRepository).findByCategoryId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter products by price range")
    void shouldFilterProductsByPriceRange() {
        // Given
        ProductFilterRequest request = ProductFilterRequest.builder()
                .minPrice(new BigDecimal("400000"))
                .maxPrice(new BigDecimal("600000"))
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        List<Product> products = Collections.singletonList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productRepository.findByBasePriceBetween(any(BigDecimal.class), 
                any(BigDecimal.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        List<ProductImage> images = Collections.singletonList(testImage);
        when(productImageRepository.findByProductId(anyLong())).thenReturn(images);
        when(skuRepository.findByProductId(anyLong())).thenReturn(Collections.singletonList(testSku));

        // When
        PageResponse<ProductListResponse> response = productService.getProducts(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(productRepository).findByBasePriceBetween(
                eq(new BigDecimal("400000")),
                eq(new BigDecimal("600000")),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Should get product detail by ID")
    void shouldGetProductDetailById() {
        // Given
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(skuRepository.findByProductId(productId)).thenReturn(Collections.singletonList(testSku));
        when(productImageRepository.findByProductId(productId))
                .thenReturn(Collections.singletonList(testImage));

        // When
        ProductDetailResponse response = productService.getProductDetail(productId);

        // Then
        assertThat(response).isNotNull();

        verify(productRepository).findById(productId);
        verify(skuRepository).findByProductId(productId);
        verify(productImageRepository).findByProductId(productId);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void shouldThrowExceptionWhenProductNotFoundById() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductDetail(productId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should get product detail by slug")
    void shouldGetProductDetailBySlug() {
        // Given
        String slug = "test-product";

        when(productRepository.findBySlug(slug)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(skuRepository.findByProductId(anyLong())).thenReturn(Collections.singletonList(testSku));
        when(productImageRepository.findByProductId(anyLong()))
                .thenReturn(Collections.singletonList(testImage));

        // When
        ProductDetailResponse response = productService.getProductDetailBySlug(slug);

        // Then
        assertThat(response).isNotNull();

        verify(productRepository).findBySlug(slug);
    }

    @Test
    @DisplayName("Should throw exception when product not found by slug")
    void shouldThrowExceptionWhenProductNotFoundBySlug() {
        // Given
        String slug = "nonexistent-slug";
        when(productRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductDetailBySlug(slug))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

        verify(productRepository).findBySlug(slug);
    }

    @Test
    @DisplayName("Should get all categories")
    void shouldGetAllCategories() {
        // Given
        List<Category> categories = Collections.singletonList(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<CategoryResponse> response = productService.getAllCategories();

        // Then
        assertThat(response).isNotNull();

        verify(categoryRepository).findAll();
    }
}
