package wandererpi.lbs.service;

import wandererpi.lbs.entity.Order;
import wandererpi.lbs.enums.OrderStatus;

public interface EmailService {
    
    /**
     * Send order confirmation email with tracking link
     */
    void sendOrderConfirmation(Order order, String rawToken);
    
    /**
     * Send order status update email
     */
    void sendOrderStatusUpdate(Order order, OrderStatus oldStatus, OrderStatus newStatus);

    /**
     * Send OTP to whatever account requesting this
     * @param to send to whom?
     * @param fullName fullName of recipient
     * @param otpCode otpCode for recipient's account
     */
    void sendOtpEmail(String to, String fullName, String otpCode);
}
