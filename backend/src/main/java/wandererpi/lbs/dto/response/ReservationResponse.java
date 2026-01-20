package wandererpi.lbs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import wandererpi.lbs.enums.ReservationStatus;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    
    private Long reservationId;
    private Long skuId;
    private String skuCode;
    private Integer quantity;
    private ReservationStatus status;
    private Instant expiresAt;
}
