package wandererpi.lbs.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates One-Time Password (OTP) codes.
 * Extracted from AuthServiceImpl to follow Single Responsibility Principle.
 */
@Component
public class OtpGenerator {

    private static final int OTP_LENGTH = 6;
    private static final int MIN_OTP = 100000;
    private static final int MAX_OTP = 999999;

    private final SecureRandom random;

    public OtpGenerator() {
        // Use SecureRandom for better security than Random
        this.random = new SecureRandom();
    }

    /**
     * Generate a 6-digit OTP code.
     *
     * @return 6-digit OTP as string (e.g., "123456")
     */
    public String generate() {
        int otp = MIN_OTP + random.nextInt(MAX_OTP - MIN_OTP + 1);
        return String.valueOf(otp);
    }

    /**
     * Generate OTP with custom length.
     *
     * @param length desired OTP length
     * @return OTP string with specified length
     */
    public String generate(int length) {
        if (length < 4 || length > 8) {
            throw new IllegalArgumentException("OTP length must be between 4 and 8");
        }

        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int otp = min + random.nextInt(max - min + 1);

        return String.valueOf(otp);
    }
}