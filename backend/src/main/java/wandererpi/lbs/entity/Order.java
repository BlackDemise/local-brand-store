package wandererpi.lbs.entity;

import jakarta.persistence.*;
import lombok.*;
import wandererpi.lbs.entity.base.BaseEntity;
import wandererpi.lbs.enums.OrderStatus;
import wandererpi.lbs.enums.PaymentMethod;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {
    /* Used for order tracking without login */
    @Column(nullable = false, unique = true)
    private String trackingToken;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private String customerName;
    private String customerPhone;
    private String customerEmail;

    @Column(columnDefinition = "TEXT")
    private String shippingAddr;

    @Column(columnDefinition = "TEXT")
    private String note;
}

