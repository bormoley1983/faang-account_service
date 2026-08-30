package faang.school.accountservice.service;

import faang.school.accountservice.exeption.AccountNotFoundException;
import faang.school.accountservice.exeption.InsufficientFundsException;
import faang.school.accountservice.exeption.SavingsAccountNotFoundException;
import faang.school.accountservice.model.Account;

import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.model.Tariff;
import jakarta.persistence.EntityNotFoundException;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SavingAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private SavingsAccountService savingsAccountService;

    @Test
    void openSavingsAccountTest() {
        Long accountId = 1L;
        Long tariffId = 1L;
        Account account = new Account();
        Tariff tariff = new Tariff();
        tariff.setRateHistory(List.of(new BigDecimal("5")));

        Mockito.when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));
        ArgumentCaptor<SavingsAccount> savingsAccountArgumentCaptor = ArgumentCaptor.forClass(SavingsAccount.class);
        SavingsAccount savingsAccount = SavingsAccount.builder()
                .balance(BigDecimal.ZERO)
                .account(account)
                .build();

        Mockito.when(savingsAccountRepository.save(Mockito.any(SavingsAccount.class)))
                .thenReturn(savingsAccount);

        SavingsAccount result = savingsAccountService.openSavingsAccount(accountId, tariffId);
        Mockito.verify(savingsAccountRepository)
                .save(savingsAccountArgumentCaptor.capture());
        SavingsAccount capturedValue = savingsAccountArgumentCaptor.getValue();

        Assertions.assertEquals(savingsAccount.getBalance(), capturedValue.getBalance());
        Assertions.assertEquals(savingsAccount.getAccount(), capturedValue.getAccount());

        Assertions.assertEquals(savingsAccount, result);
    }

    @Test
    void openSavingsAccount_NotFoundTest() {
        Long accountId = 1L;
        Long tariffId = 1L;
        Mockito.when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(AccountNotFoundException.class,
                () -> savingsAccountService.openSavingsAccount(accountId, tariffId));
    }

    @Test
    void getSavingsAccountTest() {
        Long accountId = 1L;
        SavingsAccount savingsAccount = new SavingsAccount();
        Mockito.when(savingsAccountRepository.findById(accountId))
                .thenReturn(Optional.of(savingsAccount));

        SavingsAccount result = savingsAccountService.getSavingsAccount(accountId);

        Assertions.assertEquals(savingsAccount, result);
        Mockito.verify(savingsAccountRepository, Mockito.times(1)).findById(accountId);
    }

    @Test
    void getSavingsAccount_NotFoundTest() {
        Long accountId = 1L;
        Mockito.when(savingsAccountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(SavingsAccountNotFoundException.class,
                () -> savingsAccountService.getSavingsAccount(accountId));
    }

    @Test
    void depositTest() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        SavingsAccount savingsAccount = new SavingsAccount();
        Mockito.when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(savingsAccount));
        savingsAccountService.deposit(accountId, amount);
        ArgumentCaptor<SavingsAccount> savingsAccountArgumentCaptor = ArgumentCaptor.forClass(SavingsAccount.class);
        Mockito.verify(savingsAccountRepository).save(savingsAccountArgumentCaptor.capture());
        SavingsAccount capturedValue = savingsAccountArgumentCaptor.getValue();
        Assertions.assertEquals(savingsAccount.getBalance(), capturedValue.getBalance());
        Assertions.assertEquals(savingsAccount.getAccount(), capturedValue.getAccount());
    }

    @Test
    void withdraw() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("5.00");
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setBalance(new BigDecimal("10.00"));
        Mockito.when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(savingsAccount));
        savingsAccountService.withdraw(accountId, amount);
        ArgumentCaptor<SavingsAccount> savingsAccountArgumentCaptor = ArgumentCaptor.forClass(SavingsAccount.class);
        Mockito.verify(savingsAccountRepository).save(savingsAccountArgumentCaptor.capture());
        SavingsAccount capturedValue = savingsAccountArgumentCaptor.getValue();
        Assertions.assertEquals(savingsAccount.getBalance(), capturedValue.getBalance());
        Assertions.assertEquals(savingsAccount.getAccount(), capturedValue.getAccount());
    }

    @Test
    void withdraw_exceptionTest() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("5.00");
        SavingsAccount savingsAccount = new SavingsAccount();
        Mockito.when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(savingsAccount));

        Assertions.assertThrows(InsufficientFundsException.class,
                () -> savingsAccountService.withdraw(accountId, amount));
    }

    @Test
    void withdraw_NotFoundException() {
        Long accountId = 1L;
        Mockito.when(savingsAccountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(SavingsAccountNotFoundException.class,
                () -> savingsAccountService.getSavingsAccount(accountId));
    }

    @Test
    void openSavingsAccount_whenTariffMissing_throwsEntityNotFound() {
        Long accountId = 1L;
        Long tariffId = 99L;
        Account account = new Account();
        Mockito.when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> savingsAccountService.openSavingsAccount(accountId, tariffId));
    }

    @Test
    void openSavingsAccount_whenTariffHasNoRateHistory_throwsIllegalState() {
        Long accountId = 1L;
        Long tariffId = 1L;
        Account account = new Account();
        Tariff tariff = new Tariff();
        tariff.setRateHistory(List.of());
        Mockito.when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> savingsAccountService.openSavingsAccount(accountId, tariffId));

        Assertions.assertEquals("Tariff has no rate history", exception.getMessage());
    }

    @Test
    void openSavingsAccount_whenTariffRateHistoryNull_throwsIllegalState() {
        Long accountId = 1L;
        Long tariffId = 1L;
        Account account = new Account();
        Tariff tariff = new Tariff();
        tariff.setRateHistory(null);
        Mockito.when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));

        Assertions.assertThrows(IllegalStateException.class,
                () -> savingsAccountService.openSavingsAccount(accountId, tariffId));
    }

    @Test
    void getSavingsAccountByOwnerId_whenExists_returnsAccount() {
        Long ownerId = 7L;
        SavingsAccount savingsAccount = new SavingsAccount();
        Mockito.when(savingsAccountRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(savingsAccount));

        SavingsAccount result = savingsAccountService.getSavingsAccountByOwnerId(ownerId);

        Assertions.assertEquals(savingsAccount, result);
    }

    @Test
    void getSavingsAccountByOwnerId_whenMissing_throwsNotFound() {
        Long ownerId = 7L;
        Mockito.when(savingsAccountRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());

        Assertions.assertThrows(SavingsAccountNotFoundException.class,
                () -> savingsAccountService.getSavingsAccountByOwnerId(ownerId));
    }

    @Test
    void deposit_whenNullAmount_throwsIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> savingsAccountService.deposit(1L, null));
    }

    @Test
    void deposit_whenZeroAmount_throwsIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> savingsAccountService.deposit(1L, BigDecimal.ZERO));
    }

    @Test
    void deposit_whenNegativeAmount_throwsIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> savingsAccountService.deposit(1L, new BigDecimal("-1.00")));
    }

    @Test
    void withdraw_whenNullAmount_throwsIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> savingsAccountService.withdraw(1L, null));
    }

    @Test
    void withdraw_whenZeroAmount_throwsIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> savingsAccountService.withdraw(1L, BigDecimal.ZERO));
    }

    @Test
    void withdraw_whenNegativeAmount_throwsIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> savingsAccountService.withdraw(1L, new BigDecimal("-1.00")));
    }

    @Test
    void deposit_whenValidAmount_increasesBalance() {
        Long accountId = 1L;
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setBalance(new BigDecimal("10.00"));
        Mockito.when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(savingsAccount));

        savingsAccountService.deposit(accountId, new BigDecimal("5.00"));

        Assertions.assertEquals(new BigDecimal("15.00"), savingsAccount.getBalance());
    }

    @Test
    void withdraw_whenExactBalance_allowsFullWithdrawal() {
        Long accountId = 1L;
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setBalance(new BigDecimal("10.00"));
        Mockito.when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(savingsAccount));

        savingsAccountService.withdraw(accountId, new BigDecimal("10.00"));

        Assertions.assertEquals(0, savingsAccount.getBalance().compareTo(BigDecimal.ZERO));
    }
}
