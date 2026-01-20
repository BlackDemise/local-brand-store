package wandererpi.lbs.resource.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.dto.response.PageResponse;
import wandererpi.lbs.dto.response.ProductDetailResponse;
import wandererpi.lbs.dto.response.ProductListResponse;
import wandererpi.lbs.service.ProductService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductResource {
    
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<String, PageResponse<ProductListResponse>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") Integer page, // hello, this counts from 0 :)
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        ProductFilterRequest request = ProductFilterRequest.builder()
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        PageResponse<ProductListResponse> response = productService.getProducts(request);
        
        return ResponseEntity.ok(
            ApiResponse.<String, PageResponse<ProductListResponse>>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Products retrieved successfully")
                .result(response)
                .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<String, ProductDetailResponse>> getProductById(@PathVariable Long id) {
        ProductDetailResponse response = productService.getProductDetail(id);
        
        return ResponseEntity.ok(
            ApiResponse.<String, ProductDetailResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Product detail retrieved successfully")
                .result(response)
                .build()
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<String, ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        ProductDetailResponse response = productService.getProductDetailBySlug(slug);
        
        return ResponseEntity.ok(
            ApiResponse.<String, ProductDetailResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Product detail retrieved successfully")
                .result(response)
                .build()
        );
    }
}
