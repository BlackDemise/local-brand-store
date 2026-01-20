package wandererpi.lbs.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.Reservation;
import wandererpi.lbs.enums.ReservationStatus;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCartIdAndStatus(Long cartId, ReservationStatus status);
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant dateTime);
}
