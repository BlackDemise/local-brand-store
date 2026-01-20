package wandererpi.lbs.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import wandererpi.lbs.entity.Order;
import wandererpi.lbs.enums.PaymentMethod;
import wandererpi.lbs.exception.ApplicationException;
import wandererpi.lbs.enums.ErrorCode;
import wandererpi.lbs.repository.jpa.OrderRepository;
import wandererpi.lbs.util.VietQRUtil;
import wandererpi.lbs.config.PaymentConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Test view controller for rendering QR codes without frontend
 * For development/testing purposes only
 */
@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final OrderRepository orderRepository;
    private final PaymentConfig paymentConfig;

    /**
     * Display QR code for an order
     * URL: <a href="http://localhost:8080/test/qr/">...</a>{orderId}
     */
    @GetMapping("/qr/{orderId}")
    public String showQrCode(@PathVariable Long orderId, Model model) {
        log.info("Displaying QR code for order ID: {}", orderId);

        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        // Check if order is bank transfer
        if (order.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            model.addAttribute("error", "This order does not use BANK_TRANSFER payment method");
            return "error";
        }

        // Generate VietQR string
        String transferMessage = "ORDER-" + order.getId();
        String qrContent = VietQRUtil.generateVietQR(
                paymentConfig.getBankTransfer().getBankCode(),
                paymentConfig.getBankTransfer().getAccountNo(),
                order.getTotalAmount().longValue(),
                transferMessage
        );

        // Generate QR code image as base64
        String qrImageBase64;
        try {
            qrImageBase64 = generateQRCodeBase64(qrContent, 400, 400);
        } catch (Exception e) {
            log.error("Failed to generate QR code image", e);
            model.addAttribute("error", "Failed to generate QR code");
            return "error";
        }

        // Add data to model
        model.addAttribute("orderId", order.getId());
        model.addAttribute("trackingToken", order.getTrackingToken());
        model.addAttribute("status", order.getStatus());
        model.addAttribute("totalAmount", order.getTotalAmount());
        model.addAttribute("bankCode", paymentConfig.getBankTransfer().getBankCode());
        model.addAttribute("accountNo", paymentConfig.getBankTransfer().getAccountNo());
        model.addAttribute("accountName", paymentConfig.getBankTransfer().getAccountName());
        model.addAttribute("transferMessage", transferMessage);
        model.addAttribute("qrContent", qrContent);
        model.addAttribute("qrImage", qrImageBase64);

        return "payment-qr";
    }

    /**
     * Generate QR code image as base64 string
     */
    private String generateQRCodeBase64(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }
}