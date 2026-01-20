package wandererpi.lbs.dto.response;

import lombok.*;
import wandererpi.lbs.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {
    
    private Long orderId;
    private String trackingToken;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String customerName;
    private Instant createdAt;
}
