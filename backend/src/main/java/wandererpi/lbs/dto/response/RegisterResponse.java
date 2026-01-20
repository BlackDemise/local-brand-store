package wandererpi.lbs.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String email;
    private String message;
    private Long otpExpirySeconds; // Time until OTP expires
}
