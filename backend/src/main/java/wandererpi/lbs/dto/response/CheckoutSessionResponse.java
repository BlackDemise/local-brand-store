package wandererpi.lbs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionResponse {
    
    private Long cartId;
    
    @Builder.Default
    private List<ReservationResponse> reservations = new ArrayList<>();
    
    private Instant expiresAt;
    
    @Builder.Default
    private Integer expirationSeconds = 900;
    
    private BigDecimal totalAmount;
}
