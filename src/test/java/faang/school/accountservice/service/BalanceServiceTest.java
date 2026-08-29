package faang.school.accountservice.service;

import faang.school.accountservice.exeption.BalanceNotFoundException;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

class BalanceServiceTest {

    private static final long ACCOUNT_ID = 1L;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private BalanceService balanceService;

    private Balance balance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        balance = Balance.builder()
                .actualBalance(new BigDecimal("100.00"))
                .authorizedBalance(new BigDecimal("40.00"))
                .build();
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(balance);
    }

    @Test
    void commitAuthorizationRejectsAmountGreaterThanAuthorizedBalance() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> balanceService.commitAuthorization(ACCOUNT_ID, new BigDecimal("40.01"))
        );

        assertEquals("Insufficient authorized funds", exception.getMessage());
        assertEquals(new BigDecimal("40.00"), balance.getAuthorizedBalance());
        assertEquals(new BigDecimal("100.00"), balance.getActualBalance());
        verify(balanceRepository, never()).save(balance);
    }

    @Test
    void commitAuthorizationAllowsEntireAuthorizedBalance() {
        when(balanceRepository.save(balance)).thenReturn(balance);

        Balance result = balanceService.commitAuthorization(ACCOUNT_ID, new BigDecimal("40.00"));

        assertSame(balance, result);
        assertEquals(new BigDecimal("0.00"), balance.getAuthorizedBalance());
        assertEquals(new BigDecimal("60.00"), balance.getActualBalance());
        verify(balanceRepository).save(balance);
    }

    @Test
    void cancelAuthorizationRejectsAmountGreaterThanAuthorizedBalance() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> balanceService.cancelAuthorization(ACCOUNT_ID, new BigDecimal("40.01"))
        );

        assertEquals("Insufficient authorized funds", exception.getMessage());
        assertEquals(new BigDecimal("40.00"), balance.getAuthorizedBalance());
        assertEquals(new BigDecimal("100.00"), balance.getActualBalance());
        verify(balanceRepository, never()).save(balance);
    }

    @Test
    void cancelAuthorizationAllowsEntireAuthorizedBalance() {
        when(balanceRepository.save(balance)).thenReturn(balance);

        Balance result = balanceService.cancelAuthorization(ACCOUNT_ID, new BigDecimal("40.00"));

        assertSame(balance, result);
        assertEquals(new BigDecimal("0.00"), balance.getAuthorizedBalance());
        assertEquals(new BigDecimal("100.00"), balance.getActualBalance());
        verify(balanceRepository).save(balance);
    }

    @Test
    void authorizeAmountRejectsZeroAmountBeforeLoadingBalance() {
        assertInvalidAmount(() -> balanceService.authorizeAmount(ACCOUNT_ID, BigDecimal.ZERO));
    }

    @Test
    void commitAuthorizationRejectsNegativeAmountBeforeLoadingBalance() {
        assertInvalidAmount(() -> balanceService.commitAuthorization(ACCOUNT_ID, new BigDecimal("-0.01")));
    }

    @Test
    void cancelAuthorizationRejectsNullAmountBeforeLoadingBalance() {
        assertInvalidAmount(() -> balanceService.cancelAuthorization(ACCOUNT_ID, null));
    }

    @Test
    void creditBalanceRejectsNegativeAmountBeforeLoadingBalance() {
        assertInvalidAmount(() -> balanceService.creditBalance(ACCOUNT_ID, new BigDecimal("-10.00")));
    }

    @Test
    void debitBalanceRejectsZeroAmountBeforeLoadingBalance() {
        assertInvalidAmount(() -> balanceService.debitBalance(ACCOUNT_ID, BigDecimal.ZERO));
    }

    @Test
    void getBalanceByAccountIdThrowsTypedExceptionWhenBalanceDoesNotExist() {
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(null);

        BalanceNotFoundException exception = assertThrows(
                BalanceNotFoundException.class,
                () -> balanceService.getBalanceByAccountId(ACCOUNT_ID)
        );

        assertEquals("Balance not found for account id: " + ACCOUNT_ID, exception.getMessage());
    }

    @Test
    void balanceMutationThrowsTypedExceptionBeforeDereferencingMissingBalance() {
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(null);

        BalanceNotFoundException exception = assertThrows(
                BalanceNotFoundException.class,
                () -> balanceService.creditBalance(ACCOUNT_ID, BigDecimal.ONE)
        );

        assertEquals("Balance not found for account id: " + ACCOUNT_ID, exception.getMessage());
        verify(balanceRepository, never()).save(balance);
    }

    private void assertInvalidAmount(Runnable operation) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, operation::run);

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verify(balanceRepository, never()).findByAccountId(anyLong());
    }
}
