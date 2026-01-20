package wandererpi.lbs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private Long cartId;
    private String cartToken;
    
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();
    
    private BigDecimal subtotal;
    
    @Builder.Default
    private List<StockWarning> warnings = new ArrayList<>();
}
