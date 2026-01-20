package wandererpi.lbs.service;

import jakarta.servlet.http.HttpServletResponse;
import wandererpi.lbs.dto.request.AuthRequest;
import wandererpi.lbs.dto.request.RegisterRequest;
import wandererpi.lbs.dto.request.VerifyOtpRequest;
import wandererpi.lbs.dto.response.AuthResponse;
import wandererpi.lbs.dto.response.RegisterResponse;

public interface AuthenticationService {
    AuthResponse login(AuthRequest request, HttpServletResponse response);
    void logout(String authorizationHeader, String refreshToken, HttpServletResponse response);
    boolean introspect(String authorizationHeader);
    AuthResponse refresh(String refreshToken, HttpServletResponse response);
    RegisterResponse register(RegisterRequest request);
    boolean verifyOtp(VerifyOtpRequest request, HttpServletResponse response);
}
