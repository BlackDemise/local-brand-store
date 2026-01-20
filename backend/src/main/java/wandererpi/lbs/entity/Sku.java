package wandererpi.lbs.entity;

import jakarta.persistence.*;
import lombok.*;
import wandererpi.lbs.entity.base.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sku extends BaseEntity {
    /* Many SKUs belong to one product */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String size;

    private String color;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQty;

    /* One SKU can have many SKU codes */
    @OneToMany(
            mappedBy = "sku",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<SkuCode> skuCodes = new ArrayList<>();

    /**
     * Helper method to get the primary SKU code
     *
     * @return Primary SKU code, or null if none exists
     */
    public String getPrimarySkuCode() {
        return skuCodes.stream()
                .filter(SkuCode::getIsPrimary)
                .findFirst()
                .map(SkuCode::getCode)
                .orElse(null);
    }

    /**
     * Helper method to check if a code exists for this SKU
     *
     * @param code The code to check
     * @return true if the code exists, false otherwise
     */
    public boolean hasSkuCode(String code) {
        return skuCodes.stream()
                .anyMatch(skuCode -> skuCode.getCode().equals(code));
    }
}

