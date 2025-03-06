package faang.school.accountservice.enums;

public enum AccountType {

    CHECKING(6000000000000000L),
    SAVINGS(5536000000000000L),
    CURRENCY(7000000000000000L),
    DEBIT(4200000000000000L);

    private final long baseNumber;

    AccountType(long baseNumber) {
        this.baseNumber = baseNumber;
    }

    public long getBaseNumber() {
        return baseNumber;
    }
}