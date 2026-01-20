package wandererpi.lbs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    
    private Long id;
    private Long skuId;
    private String skuCode;
    private String productName;
    private String size;
    private String color;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Integer availableStock;
    private Boolean sufficient; // quantity <= availableStock
    private BigDecimal itemTotal; // unitPrice * quantity
}
