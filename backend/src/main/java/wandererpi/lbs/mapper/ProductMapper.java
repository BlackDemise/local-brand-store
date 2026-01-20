package wandererpi.lbs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import wandererpi.lbs.dto.response.ProductListResponse;
import wandererpi.lbs.entity.Product;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {
    
    @Mapping(target = "category", source = "category")
    @Mapping(target = "primaryImageUrl", ignore = true)
    @Mapping(target = "minStock", ignore = true)
    ProductListResponse toListResponse(Product product);
    
    @Mapping(target = "category", source = "category")
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "skus", ignore = true)
    wandererpi.lbs.dto.response.ProductDetailResponse toDetailResponse(Product product);
}
