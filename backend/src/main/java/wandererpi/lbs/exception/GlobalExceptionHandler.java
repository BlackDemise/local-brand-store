package wandererpi.lbs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import wandererpi.lbs.dto.response.ApiResponse;
import wandererpi.lbs.enums.ErrorCode;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = ApplicationException.class)
    ResponseEntity<?> handleApplicationException(ApplicationException ae) {
        ErrorCode errorCode = ae.getErrorCode();
        ApiResponse<String, Object> apiResponse = ApiResponse.<String, Object>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        ApiResponse<String, Object> apiResponse = ApiResponse.<String, Object>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ErrorCode.INVALID_REQUEST.getMessage())
                .result(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    ResponseEntity<?> handleAuthenticationException() {
        ApiResponse<String, Object> apiResponse = ApiResponse.<String, Object>builder()
                .timestamp(System.currentTimeMillis())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(ErrorCode.INVALID_LOGIN_REQUEST.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}
