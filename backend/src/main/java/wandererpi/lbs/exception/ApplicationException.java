package wandererpi.lbs.exception;

import lombok.Getter;
import wandererpi.lbs.enums.ErrorCode;

@Getter
public class ApplicationException extends RuntimeException {
    ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}