package faang.school.accountservice.exeption;

public class SavingsAccountNotFoundException extends RuntimeException {
    public SavingsAccountNotFoundException(String message) {
        super(message);
    }
}
