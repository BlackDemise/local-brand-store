package wandererpi.lbs.service.validator;

import org.springframework.stereotype.Component;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.exception.ApplicationException;

import java.util.*;

/**
 * Validates order status transitions according to business rules.
 * Extracted from OrderServiceImpl to follow Single Responsibility Principle.
 */
@Component
public class OrderStatusValidator {

    private final Map<OrderStatus, Set<OrderStatus>> validTransitions;

    public OrderStatusValidator() {
        this.validTransitions = initializeValidTransitions();
    }

    /**
     * Validate that a status transition is allowed.
     *
     * @param currentStatus the current order status
     * @param newStatus the desired new status
     * @throws ApplicationException if transition is not allowed
     */
    public void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        Set<OrderStatus> allowedTransitions = validTransitions.get(currentStatus);

        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new ApplicationException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    /**
     * Get all allowed transitions from a given status.
     * Useful for UI to show available actions.
     *
     * @param currentStatus the current order status
     * @return set of allowed next statuses
     */
    public Set<OrderStatus> getAllowedTransitions(OrderStatus currentStatus) {
        return validTransitions.getOrDefault(currentStatus, Collections.emptySet());
    }

    /**
     * Check if a specific transition is allowed without throwing exception.
     *
     * @param currentStatus the current order status
     * @param newStatus the desired new status
     * @return true if transition is allowed, false otherwise
     */
    public boolean canTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        Set<OrderStatus> allowedTransitions = validTransitions.get(currentStatus);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }

    /**
     * Initialize the valid state transition map.
     * This defines the business rules for order status changes.
     *
     * @return immutable map of valid transitions
     */
    private Map<OrderStatus, Set<OrderStatus>> initializeValidTransitions() {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);

        // PENDING_PAYMENT can move to CONFIRMED or CANCELLED
        transitions.put(OrderStatus.PENDING_PAYMENT,
                EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));

        // CONFIRMED can move to SHIPPING or CANCELLED
        transitions.put(OrderStatus.CONFIRMED,
                EnumSet.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED));

        // SHIPPING can only move to DELIVERED
        transitions.put(OrderStatus.SHIPPING,
                EnumSet.of(OrderStatus.DELIVERED));

        // DELIVERED is a terminal state - no transitions
        transitions.put(OrderStatus.DELIVERED,
                EnumSet.noneOf(OrderStatus.class));

        // CANCELLED is a terminal state - no transitions
        transitions.put(OrderStatus.CANCELLED,
                EnumSet.noneOf(OrderStatus.class));

        return Collections.unmodifiableMap(transitions);
    }
}