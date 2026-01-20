package wandererpi.lbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.SkuCode;

import java.util.List;
import java.util.Optional;

public interface SkuCodeRepository extends JpaRepository<SkuCode, Long> {
    Optional<SkuCode> findByCode(String code);
    
    List<SkuCode> findBySkuId(Long skuId);
    
    Optional<SkuCode> findBySkuIdAndIsPrimaryTrue(Long skuId);
}
