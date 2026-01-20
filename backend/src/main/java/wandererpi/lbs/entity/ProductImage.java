package wandererpi.lbs.entity;

import jakarta.persistence.*;
import lombok.*;
import wandererpi.lbs.entity.base.BaseEntity;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_images_product", columnList = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {
    /* Many images belong to one product */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String imageUrl;

    /* Used for ordering images (0,1,2,...) */
    @Column(nullable = false)
    private Integer position;

    /* Quick access for thumbnail */
    @Column(nullable = false)
    private Boolean isPrimary;
}

