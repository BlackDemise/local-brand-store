package wandererpi.lbs.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.ProductImage;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
}
