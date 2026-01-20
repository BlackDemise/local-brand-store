package wandererpi.lbs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentConfig {

    private BankTransfer bankTransfer;
    private SePay sepay;

    @Data
    public static class BankTransfer {
        private String bankCode;      // e.g., "VIETCOMBANK"
        private String accountNo;     // Your merchant account
        private String accountName;   // Account holder name
    }

    @Data
    public static class SePay {
        private String webhookSecret; // Secret key for webhook validation
        private String apiKey;        // SePay API key (if needed)
    }
}