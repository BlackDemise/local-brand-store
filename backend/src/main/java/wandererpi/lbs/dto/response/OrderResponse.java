package wandererpi.lbs.dto.response;

import lombok.*;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    
    private Long orderId;
    private String trackingToken;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private String note;
    private List<OrderItemResponse> items;
    private Instant createdAt;
    // Payment information for BANK_TRANSFER
    private BankTransferInfo bankTransferInfo;

    @Data
    @Builder
    public static class BankTransferInfo {
        private String qrContent;      // VietQR string
        private String qrImageUrl;     // Optional: URL to QR image
        private String bankCode;       // e.g., "VIETCOMBANK"
        private String accountNo;      // Merchant account
        private String accountName;    // Account holder
        private Long amount;           // Amount to transfer
        private String transferMessage;// Message to include in transfer
    }
}
