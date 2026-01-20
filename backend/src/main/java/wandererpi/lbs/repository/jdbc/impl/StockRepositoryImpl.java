package wandererpi.lbs.repository.jdbc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import wandererpi.lbs.repository.jdbc.StockRepository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean reserveStock(Long skuId, Integer quantity) {
        log.debug("Attempting to reserve {} units of SKU {}", quantity, skuId);

        // ATOMIC OPERATION: Check and decrement in single UPDATE
        // This prevents race conditions at the database level
        int rowsAffected = jdbcTemplate.update(
                "UPDATE skus SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?",
                quantity, skuId, quantity
        );

        boolean success = rowsAffected > 0;

        if (success) {
            log.debug("Successfully reserved {} units of SKU {}", quantity, skuId);
        } else {
            log.warn("Failed to reserve {} units of SKU {} - insufficient stock", quantity, skuId);
        }

        return success;
    }

    @Override
    public void restoreStock(Long skuId, Integer quantity) {
        log.debug("Restoring {} units of SKU {}", quantity, skuId);

        int rowsAffected = jdbcTemplate.update(
                "UPDATE skus SET stock_qty = stock_qty + ? WHERE id = ?",
                quantity, skuId
        );

        if (rowsAffected > 0) {
            log.debug("Successfully restored {} units of SKU {}", quantity, skuId);
        } else {
            log.warn("Attempted to restore stock for non-existent SKU {}", skuId);
        }
    }

    @Override
    public Integer getAvailableStock(Long skuId) {
        log.debug("Fetching available stock for SKU {}", skuId);

        return jdbcTemplate.queryForObject(
                "SELECT stock_qty FROM skus WHERE id = ?",
                Integer.class,
                skuId
        );
    }
}