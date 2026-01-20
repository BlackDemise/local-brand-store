package wandererpi.lbs.resource.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wandererpi.lbs.dto.request.StartCheckoutRequest;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.dto.response.CheckoutSessionResponse;
import wandererpi.lbs.service.ReservationService;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutResource {
    
    private final ReservationService reservationService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<String, CheckoutSessionResponse>> startCheckout(
            @Valid @RequestBody StartCheckoutRequest request) {
        
        CheckoutSessionResponse response = reservationService.startCheckout(request);
        
        return ResponseEntity.ok(
            ApiResponse.<String, CheckoutSessionResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Checkout started successfully. Stock reserved for 15 minutes.")
                .result(response)
                .build()
        );
    }
}
