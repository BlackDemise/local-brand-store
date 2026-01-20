package wandererpi.lbs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pending_registrations", indexes = {
        @Index(name = "idx_pending_email", columnList = "email"),
        @Index(name = "idx_otp_code", columnList = "otp_code")
})
public class PendingRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private String password; // Already hashed

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "otp_expiry", nullable = false)
    private Instant otpExpiry;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0; // Track verification attempts

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (attempts == null) {
            attempts = 0;
        }
    }
}
