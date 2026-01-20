package wandererpi.lbs.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findByCartIdAndSkuId(Long cartId, Long skuId);
    void deleteByCartId(Long cartId);
}
