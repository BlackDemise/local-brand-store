package wandererpi.lbs.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import wandererpi.lbs.enums.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "New status is required")
    private OrderStatus newStatus;
    
    private String note;
}
