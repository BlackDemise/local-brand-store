package wandererpi.lbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.dto.response.*;
import wandererpi.lbs.entity.Product;
import wandererpi.lbs.entity.ProductImage;
import wandererpi.lbs.entity.Sku;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.mapper.CategoryMapper;
import wandererpi.lbs.mapper.ProductMapper;
import wandererpi.lbs.mapper.SkuMapper;
import wandererpi.lbs.repository.CategoryRepository;
import wandererpi.lbs.repository.ProductImageRepository;
import wandererpi.lbs.repository.ProductRepository;
import wandererpi.lbs.repository.SkuRepository;
import wandererpi.lbs.service.ProductService;
import wandererpi.lbs.service.specification.product.ProductSpecificationBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SkuRepository skuRepository;
    private final ProductImageRepository productImageRepository;

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final SkuMapper skuMapper;

    private final ProductSpecificationBuilder productSpecificationBuilder;

    @Override
    @Cacheable(
            value = "products",
            key = "#request.page + ':' + #request.size + ':' + #request.sortBy + ':' + #request.sortDirection"
    )
    public PageResponse<ProductListResponse> getProducts(ProductFilterRequest request) {
        // Build the specification (Criteria API) - fuck, I hate this damn since I learnt this
        // but since it is the solution, might as well go with it
        Specification<Product> specification = productSpecificationBuilder.build(request);

        // Create pageable with sorting
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(request.getSortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Product> pagedProducts = productRepository.findAll(specification, pageable);

        // Notice how we reduce quite a lot and how we simplify the repository layer
//        // Apply filters
//        Page<Product> productPage;
//
//        if (request.getCategoryId() != null && request.getMinPrice() != null && request.getMaxPrice() != null) {
//            // Filter by category and price range
//            productPage = productRepository.findByCategoryId(request.getCategoryId(), pageable)
//                    .map(product -> {
//                        if (product.getBasePrice().compareTo(request.getMinPrice()) >= 0
//                                && product.getBasePrice().compareTo(request.getMaxPrice()) <= 0) {
//                            return product;
//                        }
//                        return null;
//                    });
//        } else if (request.getCategoryId() != null) {
//            // Filter by category only
//            productPage = productRepository.findByCategoryId(request.getCategoryId(), pageable);
//        } else if (request.getMinPrice() != null && request.getMaxPrice() != null) {
//            // Filter by price range only
//            productPage = productRepository.findByBasePriceBetween(
//                    request.getMinPrice(),
//                    request.getMaxPrice(),
//                    pageable
//            );
//        } else {
//            // No filters, get all products
//            productPage = productRepository.findAll(pageable);
//        }

        // Convert to response
        List<ProductListResponse> content = pagedProducts.getContent().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductListResponse>builder()
                .content(content)
                .currentPage(pagedProducts.getNumber())
                .totalPages(pagedProducts.getTotalPages())
                .totalElements(pagedProducts.getTotalElements())
                .pageSize(pagedProducts.getSize())
                .hasNext(pagedProducts.hasNext())
                .hasPrevious(pagedProducts.hasPrevious())
                .build();
    }

    @Override
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

        return convertToDetailResponse(product);
    }

    @Override
    public ProductDetailResponse getProductDetailBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

        return convertToDetailResponse(product);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    private ProductListResponse convertToListResponse(Product product) {
        // Use mapper for basic mapping
        ProductListResponse response = productMapper.toListResponse(product);

        // Get primary image (first image)
        List<ProductImage> images = productImageRepository.findByProductId(product.getId());
        String primaryImageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();
        response.setPrimaryImageUrl(primaryImageUrl);

        // Calculate minimum stock across all SKUs
        List<Sku> skus = skuRepository.findByProductId(product.getId());
        Integer minStock = skus.stream()
                .map(Sku::getStockQty)
                .min(Integer::compareTo)
                .orElse(0);
        response.setMinStock(minStock);

        return response;
    }

    private ProductDetailResponse convertToDetailResponse(Product product) {
        // Use mapper for basic mapping
        ProductDetailResponse response = productMapper.toDetailResponse(product);

        // Get all images
        List<ProductImage> images = productImageRepository.findByProductId(product.getId());
        List<String> imageUrls = images.stream()
                .sorted(Comparator.comparing(ProductImage::getPosition))
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);

        // Get all SKUs
        List<Sku> skus = skuRepository.findByProductId(product.getId());
        List<SkuResponse> skuResponses = skus.stream()
                .map(skuMapper::toResponse)
                .collect(Collectors.toList());
        response.setSkus(skuResponses);

        return response;
    }
}
