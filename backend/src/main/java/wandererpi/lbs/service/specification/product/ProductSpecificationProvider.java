package wandererpi.lbs.service.specification.product;

import org.springframework.data.jpa.domain.Specification;
import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.entity.Product;

public interface ProductSpecificationProvider {
    Specification<Product> toSpecification(ProductFilterRequest request);
}

