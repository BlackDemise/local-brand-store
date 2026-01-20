package wandererpi.lbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.OrderHistory;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByOrderIdOrderByUpdatedAtDesc(Long orderId);
}
