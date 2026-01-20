package wandererpi.lbs.repository.jdbc;

/**
 * Repository for atomic stock operations.
 * This abstraction decouples services from JDBC implementation details.
 */
public interface StockRepository {

    /**
     * Atomically reserve stock by decrementing available quantity.
     * Uses database-level atomic operation to prevent race conditions.
     *
     * @param skuId SKU identifier
     * @param quantity amount to reserve
     * @return true if reservation succeeded, false if insufficient stock
     */
    boolean reserveStock(Long skuId, Integer quantity);

    /**
     * Restore stock by incrementing available quantity.
     * Used when orders are cancelled or reservations expire.
     *
     * @param skuId SKU identifier
     * @param quantity amount to restore
     */
    void restoreStock(Long skuId, Integer quantity);

    /**
     * Get current available stock for a SKU.
     *
     * @param skuId SKU identifier
     * @return current stock quantity
     */
    Integer getAvailableStock(Long skuId);
}
