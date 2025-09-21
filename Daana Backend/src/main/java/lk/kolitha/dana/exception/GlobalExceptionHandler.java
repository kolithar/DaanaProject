package lk.kolitha.dana.exception;

import lk.kolitha.dana.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        CommonResponse<Map<String, String>> response = new CommonResponse<>(
                false,
                "Validation failed",
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonResponse<String>> handleBadCredentials(BadCredentialsException ex) {
        CommonResponse<String> response = new CommonResponse<>(
                false,
                "Invalid email or password",
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(CustomServiceException.class)
    public ResponseEntity<CommonResponse<String>> handleCustomServiceException(CustomServiceException ex) {
        CommonResponse<String> response = new CommonResponse<>(
                false,
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<String>> handleRuntimeException(RuntimeException ex) {
        CommonResponse<String> response = new CommonResponse<>(
                false,
                ex.getMessage(),
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<String>> handleGenericException(Exception ex) {
        CommonResponse<String> response = new CommonResponse<>(
                false,
                "An unexpected error occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
