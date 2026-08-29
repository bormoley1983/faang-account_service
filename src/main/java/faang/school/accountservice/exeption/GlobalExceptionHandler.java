package faang.school.accountservice.exeption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<Class<? extends Exception>, ErrorMessages> ERROR_STATUS_MAP = new HashMap<>();

    static {
        ERROR_STATUS_MAP.put(MethodArgumentNotValidException.class, ErrorMessages.BAD_REQUEST);
        ERROR_STATUS_MAP.put(IllegalArgumentException.class, ErrorMessages.BAD_REQUEST);
        ERROR_STATUS_MAP.put(NoSuchElementException.class, ErrorMessages.NOT_FOUND);
        ERROR_STATUS_MAP.put(IllegalStateException.class, ErrorMessages.CONFLICT);
        ERROR_STATUS_MAP.put(Exception.class, ErrorMessages.INTERNAL_SERVER_ERROR);
        ERROR_STATUS_MAP.put(AccountNotFoundException.class, ErrorMessages.NOT_FOUND);
        ERROR_STATUS_MAP.put(SavingsAccountNotFoundException.class, ErrorMessages.NOT_FOUND);
        ERROR_STATUS_MAP.put(TariffNotFound.class, ErrorMessages.NOT_FOUND);

    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Resource not found");
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Access denied");
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        ErrorMessages errorMessage = ERROR_STATUS_MAP.getOrDefault(ex.getClass(), ErrorMessages.INTERNAL_SERVER_ERROR);
        log.error("Exception caught: [{}] - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        Map<String, String> response = new HashMap<>();
        response.put("error", errorMessage.getMessage());
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, errorMessage.getStatus());
    }
}