package wandererpi.lbs.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import wandererpi.lbs.service.ReservationService;

/**
 * Scheduled job for cleaning up expired reservations.
 * <p>
 * This job runs periodically to release stock from expired reservations,
 * ensuring that abandoned checkouts don't lock inventory indefinitely.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCleanupJob {

    private final ReservationService reservationService;

    /**
     * Release expired reservations every 1 minute.
     * <p>
     * This ensures abandoned checkouts don't lock stock forever.
     * Reservations expire after 15 minutes of inactivity.
     * </p>
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void releaseExpiredReservations() {
        log.debug("Running reservation cleanup job");
        
        try {
            int releasedCount = reservationService.releaseExpiredReservations();
            
            if (releasedCount > 0) {
                log.info("Successfully released {} expired reservation(s)", releasedCount);
            } else {
                log.debug("No expired reservations found");
            }
        } catch (Exception e) {
            log.error("Error occurred during reservation cleanup", e);
        }
    }
}
