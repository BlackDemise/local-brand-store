package wandererpi.lbs.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import wandererpi.lbs.repository.jpa.BlacklistedTokenRepository;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public String extractUsername(String jwt) {
        Claims claims = extractAllClaims(jwt);
        return claims != null ? claims.getSubject() : null;
    }

    public <T> T extractClaim(String jwt,
                              Function<Claims, T> claimsResolver) {
        final Claims CLAIMS = extractAllClaims(jwt);
        return claimsResolver.apply(CLAIMS);
    }

    public String generateToken(UserDetails userDetails,
                                Long expiredTimeInMiliseconds) {
        return generateToken(new HashMap<>(), userDetails, expiredTimeInMiliseconds);
    }

    public String generateToken(Map<String, Object> extraClaims,
                                UserDetails userDetails,
                                Long expiredTimeInMiliseconds) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredTimeInMiliseconds))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String jwt,
                                UserDetails userDetails) {
        final String USERNAME = extractUsername(jwt);
        return (USERNAME.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
    }

    public boolean isTokenValid(String jwt) {
        try {
            if (blacklistedTokenRepository.existsByToken(jwt)) {
                return false;
            }

            Claims claims = extractAllClaims(jwt);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    private Claims extractAllClaims(String jwt) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSigninKey())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return null; // Return null if token is expired
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            System.out.println("Invalid token: " + e.getMessage());
            return null; // Return null if token is invalid
        } catch (Exception e) {
            System.out.println("Unexpected error while extracting claims: " + e.getMessage());
            return null; // Return null for any other unexpected issue
        }
    }

    private Key getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
