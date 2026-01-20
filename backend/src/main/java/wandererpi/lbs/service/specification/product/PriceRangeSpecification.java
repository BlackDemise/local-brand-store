package wandererpi.lbs.service.specification.product;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.entity.Product;

import java.util.ArrayList;
import java.util.List;

@Component
public class PriceRangeSpecification implements ProductSpecificationProvider {

    @Override
    public Specification<Product> toSpecification(ProductFilterRequest request) {
        List<Predicate> predicates = new ArrayList<>();

        return (root, query, cb) -> {
            if (request.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("basePrice"), request.getMinPrice())
                );
            }
            if (request.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("basePrice"), request.getMaxPrice())
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

