package wandererpi.lbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.Sku;

import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Long> {
    List<Sku> findByProductId(Long productId);
}
