package wandererpi.lbs.enums;

public enum PaymentStatus {
    PENDING,      // QR generated, awaiting payment
    PAID,         // Webhook confirmed payment
    FAILED,       // Payment timeout or mismatch
    CANCELLED     // Order cancelled before payment
}
