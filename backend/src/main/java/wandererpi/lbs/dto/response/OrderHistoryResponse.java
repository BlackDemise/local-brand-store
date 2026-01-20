package wandererpi.lbs.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryResponse {
    
    private Long id;
    private String oldStatus;
    private String newStatus;
    private String note;
    private Instant changedAt;
}
