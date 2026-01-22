package wandererpi.lbs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import wandererpi.lbs.entity.Order;
import wandererpi.lbs.entity.OrderItem;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.repository.jpa.OrderItemRepository;
import wandererpi.lbs.service.EmailService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final OrderItemRepository orderItemRepository;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Async
    public void sendOrderConfirmation(Order order, String rawToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getCustomerEmail());
            message.setSubject("Order Confirmation - #" + rawToken);
            message.setText(buildOrderConfirmationEmail(order, rawToken));
            
            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", order.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", order.getCustomerEmail(), e);
            // Don't throw exception - email failure should not fail order creation
        }
    }

    @Override
    @Async
    public void sendOrderStatusUpdate(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getCustomerEmail());
            message.setSubject("Order Status Update - #" + order.getTrackingToken());
            message.setText(buildOrderStatusUpdateEmail(order, oldStatus, newStatus));
            
            mailSender.send(message);
            log.info("Order status update email sent to: {}", order.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send order status update email to: {}", order.getCustomerEmail(), e);
        }
    }

    @Override
    @Async
    public void sendOtpEmail(String to, String fullName, String otpCode) {
        String subject = "Verify Your Account";
        String body = String.format(
                """
                        Hello %s,
                        
                        Thank you for registering with Hung Store!
                        
                        Your verification code is: %s
                        
                        This code will expire in 10 minutes.
                        
                        If you did not request this code, please ignore this email.
                        
                        Best regards.
                        """,
                fullName, otpCode
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setText(body);
            message.setSubject(subject);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    // ========== Private Helper Methods ==========
    
    private String buildOrderConfirmationEmail(Order order, String rawToken) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("ORDER CONFIRMATION\n");
        sb.append("==================\n\n");
        
        sb.append("Thank you for your order!\n\n");
        
        sb.append("Order Details:\n");
        sb.append("--------------\n");
        sb.append("Order Number: ").append(rawToken).append("\n");
        sb.append("Order Date: ").append(formatInstant(order.getCreatedAt())).append("\n");
        sb.append("Status: ").append(formatStatus(order.getStatus())).append("\n");
        sb.append("Payment Method: ").append(formatPaymentMethod(order.getPaymentMethod())).append("\n\n");
        
        sb.append("Customer Information:\n");
        sb.append("---------------------\n");
        sb.append("Name: ").append(order.getCustomerName()).append("\n");
        sb.append("Phone: ").append(order.getCustomerPhone()).append("\n");
        sb.append("Email: ").append(order.getCustomerEmail()).append("\n");
        sb.append("Shipping Address: ").append(order.getShippingAddr()).append("\n\n");
        
        sb.append("Order Items:\n");
        sb.append("------------\n");
        
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            sb.append(String.format("- %s (%s, %s) x%d @ %s = %s\n",
                item.getSku().getProduct().getName(),
                item.getSku().getSize(),
                item.getSku().getColor(),
                item.getQuantity(),
                formatCurrency(item.getUnitPrice()),
                formatCurrency(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            ));
        }
        
        sb.append("\n");
        sb.append("Total Amount: ").append(formatCurrency(order.getTotalAmount())).append("\n\n");
        
        if (order.getNote() != null && !order.getNote().isEmpty()) {
            sb.append("Note: ").append(order.getNote()).append("\n\n");
        }
        
        sb.append("Track Your Order:\n");
        sb.append("-----------------\n");
        sb.append(buildTrackingUrl(order.getTrackingToken())).append("\n\n");
        
        sb.append("Thank you for shopping with us!\n\n");
        sb.append("---\n");
        sb.append("Local Brand Store\n");
        sb.append("This is an automated email. Please do not reply.\n");
        
        return sb.toString();
    }
    
    private String buildOrderStatusUpdateEmail(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        return "ORDER STATUS UPDATE\n" +
                "===================\n\n" +
                "Hello " + order.getCustomerName() + ",\n\n" +
                "Your order status has been updated.\n\n" +
                "Order Number: " + order.getTrackingToken() + "\n" +
                "Previous Status: " + formatStatus(oldStatus) + "\n" +
                "Current Status: " + formatStatus(newStatus) + "\n\n" +
                getStatusMessage(newStatus) + "\n\n" +
                "Order Details:\n" +
                "--------------\n" +
                "Total Amount: " + formatCurrency(order.getTotalAmount()) + "\n" +
                "Payment Method: " + formatPaymentMethod(order.getPaymentMethod()) + "\n\n" +
                "Track Your Order:\n" +
                "-----------------\n" +
                buildTrackingUrl(order.getTrackingToken()) + "\n\n" +
                "Thank you for your patience!\n\n" +
                "---\n" +
                "Local Brand Store\n" +
                "This is an automated email. Please do not reply.\n";
    }
    
    private String buildTrackingUrl(String trackingToken) {
        return frontendUrl + "/track/" + trackingToken;
    }
    
    private String formatStatus(OrderStatus status) {
        return switch (status) {
            case PENDING_PAYMENT -> "Pending Payment";
            case CONFIRMED -> "Confirmed";
            case SHIPPING -> "Shipping";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }
    
    private String formatPaymentMethod(wandererpi.lbs.enums.PaymentMethod method) {
        return switch (method) {
            case COD -> "Cash on Delivery";
            case BANK_TRANSFER -> "Bank Transfer";
        };
    }
    
    private String getStatusMessage(OrderStatus status) {
        return switch (status) {
            case PENDING_PAYMENT ->
                    "We are waiting for your payment confirmation. Please complete the payment to proceed with your order.";
            case CONFIRMED -> "Your order has been confirmed and is being prepared for shipment.";
            case SHIPPING -> "Your order has been shipped and is on its way to you!";
            case DELIVERED -> "Your order has been delivered. We hope you enjoy your purchase!";
            case CANCELLED ->
                    "Your order has been cancelled. If you did not request this cancellation, please contact us.";
        };
    }
    
    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
    
    private String formatInstant(java.time.Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        return formatter.format(instant);
    }
}
