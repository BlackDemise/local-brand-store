package wandererpi.lbs.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// My idea of localizing the library; and thank you, Claude, for implementing it :)
@Slf4j
public class VietQRUtil {

    // NAPAS Bank BINs (6-digit identifiers)
    private static final Map<String, String> BANK_BINS = new HashMap<>();

    static {
        BANK_BINS.put("VIETCOMBANK", "970436");
        BANK_BINS.put("VIETINBANK", "970415");
        BANK_BINS.put("TECHCOMBANK", "970407");
        BANK_BINS.put("MBBANK", "970422");
        BANK_BINS.put("ACB", "970416");
        BANK_BINS.put("VPBANK", "970432");
        BANK_BINS.put("BIDV", "970418");
        BANK_BINS.put("TPBANK", "970423");
        BANK_BINS.put("SACOMBANK", "970403");
        // Add more banks as needed
    }

    // EMVCo QR Code Field IDs
    private static final String VERSION = "00";
    private static final String INIT_METHOD = "01";
    private static final String VIETQR = "38";
    private static final String CATEGORY = "52";
    private static final String CURRENCY = "53";
    private static final String AMOUNT = "54";
    private static final String COUNTRY = "58";
    private static final String ADDITIONAL_DATA = "62";
    private static final String CRC = "63";

    // Provider Field IDs
    private static final String PROVIDER_GUID = "00";
    private static final String PROVIDER_DATA = "01";
    private static final String PROVIDER_SERVICE = "02";

    // Consumer Field IDs
    private static final String BANK_BIN = "00";
    private static final String BANK_NUMBER = "01";

    // Additional Data Field IDs
    private static final String PURPOSE_OF_TRANSACTION = "08";

    private static final String PROVIDER_VIETQR_GUID = "A000000727";
    private static final String SERVICE_BY_ACCOUNT = "QRIBFTTA";

    /**
     * Generate VietQR string for bank transfer
     *
     * @param bankCode Bank code (e.g., "VIETCOMBANK")
     * @param accountNo Bank account number
     * @param amount Amount in VND (0 for dynamic amount)
     * @param message Payment message/reference
     * @return VietQR encoded string
     */
    public static String generateVietQR(String bankCode, String accountNo, long amount, String message) {
        String bin = BANK_BINS.get(bankCode.toUpperCase());
        if (bin == null) {
            throw new IllegalArgumentException("Unknown bank code: " + bankCode);
        }

        // Build QR content
        String version = buildField(VERSION, "01");
        String initMethod = buildField(INIT_METHOD, amount > 0 ? "12" : "11");

        // Provider info (VIETQR)
        String bankBinField = buildField(BANK_BIN, bin);
        String bankNumberField = buildField(BANK_NUMBER, accountNo);
        String providerDataContent = bankBinField + bankNumberField;

        String guid = buildField(PROVIDER_GUID, PROVIDER_VIETQR_GUID);
        String providerData = buildField(PROVIDER_DATA, providerDataContent);
        String service = buildField(PROVIDER_SERVICE, SERVICE_BY_ACCOUNT);
        String providerInfo = guid + providerData + service;

        String vietqrField = buildField(VIETQR, providerInfo);

        // Transaction details
        String category = buildField(CATEGORY, "");
        String currency = buildField(CURRENCY, "704"); // VND currency code
        String amountField = amount > 0 ? buildField(AMOUNT, String.valueOf(amount)) : "";
        String country = buildField(COUNTRY, "VN");

        // Additional data (message)
        String messageField = "";
        if (message != null && !message.isEmpty()) {
            String purpose = buildField(PURPOSE_OF_TRANSACTION, message);
            messageField = buildField(ADDITIONAL_DATA, purpose);
        }

        // Build content without CRC
        String contentWithoutCrc = version + initMethod + vietqrField + category +
                currency + amountField + country + messageField;

        // Add CRC placeholder
        String contentForCrc = contentWithoutCrc + "6304";  // Include CRC field ID + length

        // Calculate and append CRC
        String crcValue = calculateCRC16(contentForCrc);
        String finalContent = contentForCrc + crcValue;

        log.info("Generated VietQR for bank: {}, account: {}, amount: {}", bankCode, accountNo, amount);
        return finalContent;
    }

    /**
     * Build QR field: ID (2 bytes) + Length (2 bytes) + Value
     */
    private static String buildField(String id, String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int length = value.length();
        return String.format("%s%02d%s", id, length, value);
    }

    /**
     * Calculate CRC-16-CCITT-FALSE checksum
     */
    private static String calculateCRC16(String data) {
        int crc = 0xFFFF;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }

        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }

    /**
     * Get bank BIN by bank code
     */
    public static String getBankBin(String bankCode) {
        return BANK_BINS.get(bankCode.toUpperCase());
    }

    /**
     * Add custom bank
     */
    public static void addBank(String bankCode, String bin) {
        BANK_BINS.put(bankCode.toUpperCase(), bin);
    }
}