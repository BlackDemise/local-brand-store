package wandererpi.lbs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import wandererpi.lbs.dto.response.SkuResponse;
import wandererpi.lbs.entity.Sku;
import wandererpi.lbs.entity.SkuCode;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SkuMapper {
    
    @Mapping(target = "available", expression = "java(sku.getStockQty() > 0)")
    @Mapping(target = "skuCodes", expression = "java(mapSkuCodes(sku))")
    @Mapping(target = "primarySkuCode", expression = "java(sku.getPrimarySkuCode())")
    SkuResponse toResponse(Sku sku);
    
    default List<String> mapSkuCodes(Sku sku) {
        return sku.getSkuCodes().stream()
                .map(SkuCode::getCode)
                .collect(Collectors.toList());
    }
}
