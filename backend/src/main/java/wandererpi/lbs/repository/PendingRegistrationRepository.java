package wandererpi.lbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.PendingRegistration;

import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByEmail(String email);
}
