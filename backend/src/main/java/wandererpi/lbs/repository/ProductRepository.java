package wandererpi.lbs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import wandererpi.lbs.entity.Product;

import java.math.BigDecimal;
import java.util.Optional;

// JpaSpecificationExecutor<T> brings in `Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable)`
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByBasePriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Optional<Product> findBySlug(String slug);
}
