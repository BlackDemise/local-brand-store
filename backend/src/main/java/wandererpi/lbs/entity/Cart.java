package wandererpi.lbs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import wandererpi.lbs.entity.base.BaseEntity;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {
    /* Used for guest checkout */
    @Column(nullable = false, unique = true)
    private String token;
}

