package wandererpi.lbs.service;

import wandererpi.lbs.dto.request.StartCheckoutRequest;
import wandererpi.lbs.dto.response.CheckoutSessionResponse;

public interface ReservationService {
    
    /**
     * Start checkout - create reservations for specified cart items
     * This is where the atomic stock reservation happens
     * 
     * @param request Contains cartToken and list of items to checkout with quantities
     */
    CheckoutSessionResponse startCheckout(StartCheckoutRequest request);
    
    /**
     * Validate reservation is still active and not expired
     */
    void validateReservation(Long cartId);
    
    /**
     * Release expired reservations (called by scheduled job)
     */
    int releaseExpiredReservations();
}
