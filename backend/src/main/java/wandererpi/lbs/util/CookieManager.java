package wandererpi.lbs.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * Manages HTTP cookie operations for authentication.
 * Extracted from AuthServiceImpl to follow Single Responsibility Principle.
 */
@Component
public class CookieManager {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 7 * 24 * 60 * 60; // 7 days
    private static final String COOKIE_PATH = "/";

    // TODO: Set to true in production with HTTPS
    private static final boolean SECURE_COOKIE = false;

    /**
     * Set refresh token cookie in HTTP response.
     *
     * @param response HTTP servlet response
     * @param refreshToken the JWT refresh token
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);  // Prevent JavaScript access (XSS protection)
        cookie.setSecure(SECURE_COOKIE);  // Send only over HTTPS in production
        cookie.setPath(COOKIE_PATH);  // Available for all paths
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE_SECONDS);

        response.addCookie(cookie);
    }

    /**
     * Clear/delete refresh token cookie from browser.
     * Used during logout.
     *
     * @param response HTTP servlet response
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(SECURE_COOKIE);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);  // Delete immediately

        response.addCookie(cookie);
    }

    /**
     * Set a generic cookie.
     *
     * @param response HTTP servlet response
     * @param name cookie name
     * @param value cookie value
     * @param maxAgeSeconds cookie lifetime in seconds
     */
    public void setCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(SECURE_COOKIE);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(maxAgeSeconds);

        response.addCookie(cookie);
    }

    /**
     * Clear a generic cookie.
     *
     * @param response HTTP servlet response
     * @param name cookie name to clear
     */
    public void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(SECURE_COOKIE);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}