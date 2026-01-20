package wandererpi.lbs.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import wandererpi.lbs.enums.PaymentMethod;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {
    
    @NotNull(message = "Cart ID is required")
    private Long cartId;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer phone is required")
    private String customerPhone;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    private String note;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
