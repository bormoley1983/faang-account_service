package faang.school.accountservice.exeption;

public class TariffNotFound extends RuntimeException {
    public TariffNotFound(String message) {
        super(message);
    }
}
