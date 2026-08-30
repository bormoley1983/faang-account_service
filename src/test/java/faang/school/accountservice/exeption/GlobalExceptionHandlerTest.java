package faang.school.accountservice.exeption;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNoSuchElementException_returns404WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNoSuchElementException(new NoSuchElementException("missing"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("error", "Resource not found")
                .containsEntry("message", "missing");
    }

    @Test
    void handleSecurityException_returns403WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleSecurityException(new SecurityException("denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
                .containsEntry("error", "Access denied")
                .containsEntry("message", "denied");
    }

    @Test
    void handleException_whenAccountNotFound_returns404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleException(new AccountNotFoundException("nope"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "nope");
    }

    @Test
    void handleException_whenIllegalArgument_returns400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleException(new IllegalArgumentException("bad"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "bad");
    }

    @Test
    void handleException_whenIllegalState_returns409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleException(new IllegalStateException("conflict"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "conflict");
    }

    @Test
    void handleException_whenUnknownException_returns500() {
        ResponseEntity<Map<String, String>> response =
                handler.handleException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "boom");
    }
}
