package wandererpi.lbs.service;

import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.dto.response.CategoryResponse;
import wandererpi.lbs.dto.response.PageResponse;
import wandererpi.lbs.dto.response.ProductDetailResponse;
import wandererpi.lbs.dto.response.ProductListResponse;

import java.util.List;

public interface ProductService {
    PageResponse<ProductListResponse> getProducts(ProductFilterRequest request);
    ProductDetailResponse getProductDetail(Long productId);
    ProductDetailResponse getProductDetailBySlug(String slug);
    List<CategoryResponse> getAllCategories();
}
