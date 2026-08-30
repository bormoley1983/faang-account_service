package faang.school.accountservice.service;

import faang.school.accountservice.exeption.BalanceNotFoundException;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void createBalance_whenNewAccount_savesBalanceLinkedToAccount() {
        Account account = Account.builder().id(ACCOUNT_ID).build();
        when(balanceRepository.existsByAccountId(ACCOUNT_ID)).thenReturn(false);
        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(account);
        when(balanceRepository.save(any(Balance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Balance created = balanceService.createBalance(ACCOUNT_ID);

        assertThat(created).isNotNull();
        assertThat(created.getAccount()).isSameAs(account);
        verify(balanceRepository).save(created);
    }

    @Test
    void createBalance_whenBalanceAlreadyExists_throwsIllegalState() {
        when(balanceRepository.existsByAccountId(ACCOUNT_ID)).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> balanceService.createBalance(ACCOUNT_ID)
        );

        assertEquals("Balance for this account already exists.", exception.getMessage());
        verify(accountService, never()).getAccount(anyLong());
        verify(balanceRepository, never()).save(any(Balance.class));
    }

    @Test
    void authorizeAmount_whenSufficientFunds_movesAmountToAuthorized() {
        when(balanceRepository.save(balance)).thenReturn(balance);

        Balance result = balanceService.authorizeAmount(ACCOUNT_ID, new BigDecimal("25.00"));

        assertSame(balance, result);
        assertEquals(new BigDecimal("65.00"), balance.getAuthorizedBalance());
        assertEquals(new BigDecimal("100.00"), balance.getActualBalance());
        verify(balanceRepository).save(balance);
    }

    @Test
    void authorizeAmount_whenExactAvailableFunds_allowsFullAuthorization() {
        when(balanceRepository.save(balance)).thenReturn(balance);

        // available = actual(100) - authorized(40) = 60
        Balance result = balanceService.authorizeAmount(ACCOUNT_ID, new BigDecimal("60.00"));

        assertSame(balance, result);
        assertEquals(new BigDecimal("100.00"), balance.getAuthorizedBalance());
        assertEquals(new BigDecimal("100.00"), balance.getActualBalance());
    }

    @Test
    void authorizeAmount_whenInsufficientFunds_throwsIllegalState() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> balanceService.authorizeAmount(ACCOUNT_ID, new BigDecimal("60.01"))
        );

        assertEquals("Insufficient funds", exception.getMessage());
        assertEquals(new BigDecimal("40.00"), balance.getAuthorizedBalance());
        assertEquals(new BigDecimal("100.00"), balance.getActualBalance());
        verify(balanceRepository, never()).save(balance);
    }

    @Test
    void creditBalance_whenValidAmount_increasesActualBalance() {
        when(balanceRepository.save(balance)).thenReturn(balance);

        Balance result = balanceService.creditBalance(ACCOUNT_ID, new BigDecimal("15.00"));

        assertSame(balance, result);
        assertEquals(new BigDecimal("115.00"), balance.getActualBalance());
        assertEquals(new BigDecimal("40.00"), balance.getAuthorizedBalance());
        verify(balanceRepository).save(balance);
    }

    @Test
    void debitBalance_whenSufficientFunds_decreasesActualBalance() {
        when(balanceRepository.save(balance)).thenReturn(balance);

        Balance result = balanceService.debitBalance(ACCOUNT_ID, new BigDecimal("30.00"));

        assertSame(balance, result);
        assertEquals(new BigDecimal("70.00"), balance.getActualBalance());
        assertEquals(new BigDecimal("40.00"), balance.getAuthorizedBalance());
        verify(balanceRepository).save(balance);
    }

    @Test
    void debitBalance_whenExactAvailableFunds_allowsFullDebit() {
        when(balanceRepository.save(balance)).thenReturn(balance);

        // available = actual(100) - authorized(40) = 60
        Balance result = balanceService.debitBalance(ACCOUNT_ID, new BigDecimal("60.00"));

        assertSame(balance, result);
        assertEquals(new BigDecimal("40.00"), balance.getActualBalance());
        assertEquals(new BigDecimal("40.00"), balance.getAuthorizedBalance());
    }

    @Test
    void debitBalance_whenInsufficientFunds_throwsIllegalState() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> balanceService.debitBalance(ACCOUNT_ID, new BigDecimal("60.01"))
        );

        assertEquals("Insufficient funds", exception.getMessage());
        assertEquals(new BigDecimal("100.00"), balance.getActualBalance());
        verify(balanceRepository, never()).save(balance);
    }

    private void assertInvalidAmount(Runnable operation) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, operation::run);

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verify(balanceRepository, never()).findByAccountId(anyLong());
    }
}
