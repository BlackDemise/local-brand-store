package wandererpi.lbs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.config.PaymentConfig;
import wandererpi.lbs.dto.request.SepayWebhookRequest;
import wandererpi.lbs.entity.Order;
import wandererpi.lbs.entity.OrderHistory;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.repository.jpa.OrderHistoryRepository;
import wandererpi.lbs.repository.jpa.OrderRepository;
import wandererpi.lbs.service.EmailService;
import wandererpi.lbs.service.PaymentService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final PaymentConfig paymentConfig;
    private final EmailService emailService;

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("ORDER-(\\d+)");

    @Override
    @Transactional
    public void processSepayWebhook(SepayWebhookRequest request, String signature) {
        log.info("SepayWebhookRequest: {}", request);
        log.info("Processing SePay webhook for transaction: {}", request.getId());

        // 1. Validate signature (if provided)
        if (signature != null && !signature.isEmpty()) {
            validateSignature(request, signature);
        }

        // 2. Validate amount (incoming transaction)
        if (request.getTransferAmount() == null || request.getTransferAmount() <= 0) {
            log.warn("Invalid webhook: no incoming amount");
            return;
        }

        // 3. Extract order ID from transaction content
        String content = request.getContent();
        if (content == null || content.isEmpty()) {
            log.warn("No transaction content provided");
            return;
        }

        Long orderId = extractOrderId(content);
        if (orderId == null) {
            log.warn("Could not extract order ID from content: {}", content);
            return;
        }

        // 4. Find order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found: {}", orderId);
                    return new ApplicationException(ErrorCode.ORDER_NOT_FOUND);
                });

        // 5. Validate order status
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Order {} is not in PENDING_PAYMENT status: {}", orderId, order.getStatus());
            return;
        }

        // 6. Validate amount
        long expectedAmount = order.getTotalAmount().longValue();
        long receivedAmount = request.getTransferAmount();

        if (receivedAmount < expectedAmount) {
            log.warn("Insufficient payment for order {}: expected={}, received={}",
                    orderId, expectedAmount, receivedAmount);
            return;
        }

        // 7. Update order status
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // 8. Create order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .oldStatus(oldStatus.name())
                .newStatus(OrderStatus.CONFIRMED.name())
                .note(String.format("Payment confirmed via SePay (Transaction: %s, Amount: %d VND)",
                        request.getId(), receivedAmount))
                .build();
        orderHistoryRepository.save(history);

        log.info("Order {} payment confirmed. Status updated: {} -> {}",
                orderId, oldStatus, OrderStatus.CONFIRMED);

        // 9. Send email notification (async)
        try {
            emailService.sendOrderStatusUpdate(order, oldStatus, OrderStatus.CONFIRMED);
        } catch (Exception e) {
            log.error("Failed to send email for order {}", orderId, e);
        }
    }

    /**
     * Extract order ID from transaction content
     * Format: "ORDER-123" or "ORDER 123" or contains "123"
     */
    private Long extractOrderId(String content) {
        Matcher matcher = ORDER_ID_PATTERN.matcher(content.toUpperCase());
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                log.error("Failed to parse order ID: {}", matcher.group(1));
            }
        }
        return null;
    }

    /**
     * Validate webhook signature using HMAC-SHA256
     */
    private void validateSignature(SepayWebhookRequest request, String signature) {
        try {
            String secret = paymentConfig.getSepay().getWebhookSecret();
            String payload = buildPayloadString(request);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = bytesToHex(hash);

            if (!expectedSignature.equalsIgnoreCase(signature)) {
                log.error("Invalid webhook signature");
                throw new ApplicationException(ErrorCode.INVALID_SIGNATURE);
            }

        } catch (Exception e) {
            log.error("Error validating signature", e);
            throw new ApplicationException(ErrorCode.INVALID_SIGNATURE);
        }
    }

    private String buildPayloadString(SepayWebhookRequest request) {
        // Build the string that was signed
        // Format depends on SePay's implementation
        return String.format("%d|%s|%d|%s",
                request.getId(),
                request.getTransactionDate(),
                request.getTransferAmount(),
                request.getContent()
        );
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}