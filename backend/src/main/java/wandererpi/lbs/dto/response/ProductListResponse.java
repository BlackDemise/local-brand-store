package wandererpi.lbs.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private Long id;
    private String name;
    private String slug;
    private BigDecimal basePrice;
    private String primaryImageUrl;
    private CategoryResponse category;
    private Integer minStock;
}
