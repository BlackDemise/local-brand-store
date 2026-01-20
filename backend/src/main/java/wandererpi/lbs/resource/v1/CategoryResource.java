package wandererpi.lbs.resource.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.dto.response.CategoryResponse;
import wandererpi.lbs.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryResource {
    
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<String, List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = productService.getAllCategories();
        
        return ResponseEntity.ok(
            ApiResponse.<String, List<CategoryResponse>>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Categories retrieved successfully")
                .result(categories)
                .build()
        );
    }
}
