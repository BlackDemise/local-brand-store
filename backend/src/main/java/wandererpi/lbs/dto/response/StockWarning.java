package wandererpi.lbs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockWarning {
    
    private Long skuId;
    private String message;
    private Integer requestedQty;
    private Integer availableQty;
}
