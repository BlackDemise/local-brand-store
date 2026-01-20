package wandererpi.lbs.resource.v1;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wandererpi.lbs.dto.request.AuthRequest;
import wandererpi.lbs.dto.request.RegisterRequest;
import wandererpi.lbs.dto.request.VerifyOtpRequest;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.dto.response.AuthResponse;
import wandererpi.lbs.dto.response.RegisterResponse;
import wandererpi.lbs.service.AuthenticationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthResource {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String, RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse registerResponse = authenticationService.register(request);

        ApiResponse<String, RegisterResponse> apiResponse = ApiResponse.<String, RegisterResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Registration initiated. Please verify OTP.")
                .result(registerResponse)
                .build();

        return ResponseEntity.status(apiResponse.getStatusCode())
                .body(apiResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String, Boolean>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletResponse response) {
        boolean isVerfiried = authenticationService.verifyOtp(request, response);

        ApiResponse<String, Boolean> apiResponse = ApiResponse.<String, Boolean>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("Registration completed successfully")
                .result(isVerfiried)
                .build();

        return ResponseEntity.status(apiResponse.getStatusCode())
                .body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String, AuthResponse>> login(
            @Valid @RequestBody(required = false) AuthRequest authRequest, HttpServletResponse response) {
        AuthResponse authResponse = authenticationService.login(authRequest, response);

        ApiResponse<String, AuthResponse> apiResponse = ApiResponse.<String, AuthResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("OK")
                .result(authResponse)
                .build();

        return ResponseEntity.status(apiResponse.getStatusCode())
                .body(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String, Void>> logout(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authenticationService.logout(authorizationHeader, refreshToken, response);

        ApiResponse<String, Void> apiResponse = ApiResponse.<String, Void>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("OK")
                .build();

        return ResponseEntity.status(apiResponse.getStatusCode())
                .body(apiResponse);
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<String, Boolean>> introspect(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        boolean isValid = authenticationService.introspect(authorizationHeader);

        ApiResponse<String, Boolean> apiResponse = ApiResponse.<String, Boolean>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("OK")
                .result(isValid)
                .build();

        return ResponseEntity.status(apiResponse.getStatusCode())
                .body(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String, AuthResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        AuthResponse authResponse = authenticationService.refresh(refreshToken, response);

        ApiResponse<String, AuthResponse> apiResponse = ApiResponse.<String, AuthResponse>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.OK.value())
                .message("OK")
                .result(authResponse)
                .build();

        return ResponseEntity.status(apiResponse.getStatusCode())
                .body(apiResponse);
    }
}