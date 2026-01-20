package wandererpi.lbs.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.Cart;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByToken(String token);
    Optional<Cart> findByCreatedBy(Long userId);
    List<Cart> findByUpdatedAtBeforeAndCreatedByIsNull(Instant cutoffDate);
}
