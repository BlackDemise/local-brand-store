package wandererpi.lbs.resource.v1;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wandererpi.lbs.dto.request.SepayWebhookRequest;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.service.PaymentService;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookResource {

    private final PaymentService paymentService;

    /**
     * SePay webhook endpoint - receives payment notifications
     * Public endpoint (no authentication)
     */
    @PostMapping("/sepay")
    public ResponseEntity<ApiResponse<String, String>> handleSepayWebhook(
            @RequestBody SepayWebhookRequest request,
            @RequestHeader(value = "X-Sepay-Signature", required = false) String signature,
            HttpServletRequest httpRequest) {
        log.info("SepayWebhookRequest: {}", request);
        log.info("Received SePay webhook: transactionId={}, amount={}, content={}",
                request.getId(), request.getTransferAmount(), request.getContent());

        try {
            // Process payment
            paymentService.processSepayWebhook(request, signature);

            return ResponseEntity.ok(
                    ApiResponse.<String, String>builder()
                            .timestamp(System.currentTimeMillis())
                            .statusCode(HttpStatus.OK.value())
                            .message("Webhook processed successfully")
                            .result("OK")
                            .build()
            );

        } catch (Exception e) {
            log.error("Error processing SePay webhook", e);
            // Return 200 even on error to avoid SePay retrying
            return ResponseEntity.ok(
                    ApiResponse.<String, String>builder()
                            .timestamp(System.currentTimeMillis())
                            .statusCode(HttpStatus.OK.value())
                            .message("Webhook received")
                            .result("OK")
                            .build()
            );
        }
    }
}