package faang.school.accountservice.exeption;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorMessages {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request data"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict in request"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");

    private final HttpStatus status;
    private final String message;

    ErrorMessages(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}