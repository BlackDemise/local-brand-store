package wandererpi.lbs.mapper;

import org.mapstruct.Mapper;
import wandererpi.lbs.dto.response.CategoryResponse;
import wandererpi.lbs.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
