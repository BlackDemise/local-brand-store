package wandererpi.lbs.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.entity.Cart;
import wandererpi.lbs.repository.CartItemRepository;
import wandererpi.lbs.repository.CartRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job for cleaning up abandoned anonymous carts.
 * <p>
 * This job runs daily to remove old anonymous carts that haven't been
 * updated in 30 days, helping to keep the database clean and performant.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartCleanupJob {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private static final int CART_RETENTION_DAYS = 30;

    /**
     * Clean up abandoned anonymous carts daily at 2:00 AM.
     * <p>
     * Removes carts that haven't been modified in the last 30 days
     * and don't belong to any registered user (anonymous carts).
     * </p>
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    @Transactional
    public void cleanupAbandonedCarts() {
        log.debug("Running cart cleanup job");
        
        try {
            Instant cutoffDate = Instant.from(LocalDateTime.now().minusDays(CART_RETENTION_DAYS));
            
            // Find old anonymous carts (where createdBy is null)
            List<Cart> oldCarts = cartRepository.findByUpdatedAtBeforeAndCreatedByIsNull(cutoffDate);
            
            if (oldCarts.isEmpty()) {
                log.debug("No abandoned carts found");
                return;
            }
            
            int deletedCount = 0;
            for (Cart cart : oldCarts) {
                try {
                    // Delete cart items first (foreign key constraint)
                    cartItemRepository.deleteByCartId(cart.getId());
                    
                    // Delete the cart
                    cartRepository.delete(cart);
                    deletedCount++;
                } catch (Exception e) {
                    log.error("Failed to delete cart with ID: {}", cart.getId(), e);
                }
            }
            
            log.info("Successfully cleaned up {} abandoned cart(s) older than {} days", 
                    deletedCount, CART_RETENTION_DAYS);
        } catch (Exception e) {
            log.error("Error occurred during cart cleanup", e);
        }
    }
}
