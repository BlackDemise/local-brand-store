package wandererpi.lbs.service.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wandererpi.lbs.dto.request.AuthRequest;
import wandererpi.lbs.dto.request.RegisterRequest;
import wandererpi.lbs.dto.request.VerifyOtpRequest;
import wandererpi.lbs.dto.response.AuthResponse;
import wandererpi.lbs.dto.response.RegisterResponse;
import wandererpi.lbs.entity.BlacklistedToken;
import wandererpi.lbs.entity.PendingRegistration;
import wandererpi.lbs.entity.User;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.enums.TokenType;
import wandererpi.lbs.enums.UserRole;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.repository.BlacklistedTokenRepository;
import wandererpi.lbs.repository.PendingRegistrationRepository;
import wandererpi.lbs.repository.UserRepository;
import wandererpi.lbs.service.AuthenticationService;
import wandererpi.lbs.service.EmailService;
import wandererpi.lbs.util.JwtUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final long OTP_VALIDITY_MINUTES = 10;

    @Override
    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        if (request == null || request.getEmail() == null || request.getPassword() == null
                || request.getEmail().isEmpty() || request.getPassword().isEmpty()) {
            throw new ApplicationException(ErrorCode.INVALID_LOGIN_REQUEST);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole());
        extraClaims.put("fullName", user.getFullName());

        String accessToken = jwtUtil.generateToken(extraClaims, user, 900000L);
        String refreshToken = jwtUtil.generateToken(user, 604800000L);

        setRefreshTokenCookie(response, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    @Override
    public void logout(String authorizationHeader, String refreshToken, HttpServletResponse response) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);

            // Prevent dirty tricks
            if (!blacklistedTokenRepository.existsByToken(accessToken)) {
                BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                        .token(accessToken)
                        .tokenType(TokenType.ACCESS)
                        .expiryDate(Instant.now().plusSeconds(900)) // 15 minutes
                        .build();
                blacklistedTokenRepository.save(blacklistedToken);
            }
        }

        // Prevent dirty tricks
        if (refreshToken != null && !blacklistedTokenRepository.existsByToken(refreshToken)) {
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(refreshToken)
                    .tokenType(TokenType.REFRESH)
                    .expiryDate(Instant.now().plusSeconds(608400)) // 7 days
                    .build();
            blacklistedTokenRepository.save(blacklistedToken);
        }

        // Remove refresh token cookie
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(false);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);
    }

    @Override
    public boolean introspect(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);

            return jwtUtil.isTokenValid(accessToken);
        }

        return false;
    }

    public AuthResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED);
        }

        if (blacklistedTokenRepository.existsByToken(refreshToken) || !jwtUtil.isTokenValid(refreshToken)) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED);
        }

        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole());
        extraClaims.put("fullName", user.getFullName());

        String newAccessToken = jwtUtil.generateToken(extraClaims, user, 900000L);
        String newRefreshToken = jwtUtil.generateToken(user, 604800000L);

        // Blacklist old refresh token (damn race condition, attempt to blacklist a refresh token twice)
        if (!blacklistedTokenRepository.existsByToken(refreshToken)) {
            blacklistedTokenRepository.save(BlacklistedToken.builder()
                    .token(refreshToken)
                    .tokenType(TokenType.REFRESH)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build());
        }

        // Set new refresh token in cookie
        setRefreshTokenCookie(response, newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate input
        if (request == null || request.getEmail() == null || request.getPassword() == null 
                || request.getFullName() == null) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        String email = request.getEmail().trim().toLowerCase();
        
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApplicationException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // Delete any existing pending registration for this email
        pendingRegistrationRepository.findByEmail(email)
                .ifPresent(pendingRegistrationRepository::delete);

        // Generate 6-digit OTP
        String otpCode = generateOtp();
        
        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        // Calculate OTP expiry
        Instant otpExpiry = Instant.now().plusSeconds(OTP_VALIDITY_MINUTES * 60);
        
        // Save pending registration
        PendingRegistration pendingRegistration = PendingRegistration.builder()
                .email(email)
                .fullName(request.getFullName())
                .password(hashedPassword)
                .otpCode(otpCode)
                .otpExpiry(otpExpiry)
                .attempts(0)
                .build();
        
        pendingRegistrationRepository.save(pendingRegistration);
        
        // Send OTP via email (async)
        emailService.sendOtpEmail(email, request.getFullName(), otpCode);
        
        return RegisterResponse.builder()
                .email(email)
                .message("OTP sent to your email. Please verify to complete registration.")
                .otpExpirySeconds(OTP_VALIDITY_MINUTES * 60)
                .build();
    }

    @Override
    @Transactional
    public boolean verifyOtp(VerifyOtpRequest request, HttpServletResponse response) {
        // Validate input
        if (request == null || request.getEmail() == null || request.getOtpCode() == null) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        String email = request.getEmail().trim().toLowerCase();
        String otpCode = request.getOtpCode().trim();
        
        // Find pending registration
        PendingRegistration pendingReg = pendingRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(ErrorCode.REGISTRATION_NOT_FOUND));
        
        // Check if OTP is expired
        if (Instant.now().isAfter(pendingReg.getOtpExpiry())) {
            pendingRegistrationRepository.delete(pendingReg);
            throw new ApplicationException(ErrorCode.OTP_EXPIRED);
        }
        
        // Verify OTP
        if (!otpCode.equals(pendingReg.getOtpCode())) {
            // Increment attempts
            pendingReg.setAttempts(pendingReg.getAttempts() + 1);
            pendingRegistrationRepository.save(pendingReg);
            throw new ApplicationException(ErrorCode.INVALID_OTP);
        }
        
        // Create user account
        User newUser = User.builder()
                .email(email)
                .fullName(pendingReg.getFullName())
                .password(pendingReg.getPassword()) // Already hashed
                .role(UserRole.CUSTOMER)
                .build();

        // Bring the user to our home :) ...
        userRepository.save(newUser);
        
        // ...and delete corresponding pending registration
        pendingRegistrationRepository.delete(pendingReg);
        
        return true;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String newRefreshToken) {
        Cookie newRefreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
        newRefreshTokenCookie.setHttpOnly(true);
        newRefreshTokenCookie.setSecure(false); // Use true for HTTPS
        newRefreshTokenCookie.setPath("/"); // Apply to all paths
        newRefreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

        response.addCookie(newRefreshTokenCookie);
    }
}
