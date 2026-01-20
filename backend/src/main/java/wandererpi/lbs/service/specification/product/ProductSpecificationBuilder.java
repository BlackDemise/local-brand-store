package wandererpi.lbs.service.specification.product;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import wandererpi.lbs.dto.request.ProductFilterRequest;
import wandererpi.lbs.entity.Product;

import java.util.List;
import java.util.Objects;

@Component
public class ProductSpecificationBuilder {

    private final List<ProductSpecificationProvider> providers;

    public ProductSpecificationBuilder(List<ProductSpecificationProvider> providers) {
        this.providers = providers;
    }

    public Specification<Product> build(ProductFilterRequest request) {
        return providers.stream()
                .map(p -> p.toSpecification(request))
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);
    }
}
