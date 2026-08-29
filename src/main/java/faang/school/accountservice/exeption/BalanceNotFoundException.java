package faang.school.accountservice.exeption;

import java.util.NoSuchElementException;

public class BalanceNotFoundException extends NoSuchElementException {

    public BalanceNotFoundException(long accountId) {
        super("Balance not found for account id: " + accountId);
    }
}
