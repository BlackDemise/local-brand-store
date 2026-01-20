package wandererpi.lbs.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_OTP(400, "Invalid OTP", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "Unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST(400, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_LOGIN_REQUEST(400, "Invalid login request", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(404, "Product not found", HttpStatus.NOT_FOUND),
    SKU_NOT_FOUND(404, "SKU not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(404, "Cart not found", HttpStatus.NOT_FOUND),
    REGISTRATION_NOT_FOUND(404, "Registration not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(404, "Cart item not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(400, "Insufficient stock", HttpStatus.BAD_REQUEST),
    INVALID_CART_TOKEN(400, "Invalid cart token", HttpStatus.BAD_REQUEST),
    RESERVATION_EXPIRED(400, "Reservation has expired", HttpStatus.BAD_REQUEST),
    NO_ACTIVE_RESERVATION(400, "No active reservation found", HttpStatus.BAD_REQUEST),
    CHECKOUT_FAILED(400, "Checkout failed", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(404, "Order not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS(400, "Invalid order status transition", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS(409, "User already exists", HttpStatus.CONFLICT),
    OTP_EXPIRED(400, "OTP expired", HttpStatus.BAD_REQUEST),
    INVALID_SIGNATURE(400, "Invalid webhook signature", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
