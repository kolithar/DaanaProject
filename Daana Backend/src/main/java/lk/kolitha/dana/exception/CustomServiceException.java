package lk.kolitha.dana.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomServiceException extends RuntimeException {
    
    private final int statusCode;
    private final HttpStatus httpStatus;
    
    public CustomServiceException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.httpStatus = HttpStatus.valueOf(statusCode);
    }
    
    public CustomServiceException(String message) {
        super(message);
        this.statusCode = 400;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public CustomServiceException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.httpStatus = HttpStatus.valueOf(statusCode);
    }
    
    public CustomServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 400;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
