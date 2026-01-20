package wandererpi.lbs.service.specification.product;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.entity.Product;

@Component
public class CategorySpecification implements ProductSpecificationProvider {

    @Override
    public Specification<Product> toSpecification(ProductFilterRequest request) {
        if (request.getCategoryId() == null) return null;

        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), request.getCategoryId());
    }
}

