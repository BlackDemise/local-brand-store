package wandererpi.lbs.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuResponse {
    private Long id;
    private List<String> skuCodes;
    private String primarySkuCode;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer stockQty;
    private Boolean available;
}
