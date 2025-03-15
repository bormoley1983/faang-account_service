package faang.school.accountservice.service;

import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FreeAccountNumberService freeAccountNumberService;

    @InjectMocks
    private AccountService accountService;

    private Long accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountId = 1L;
        account = new Account();
        account.setId(accountId);
        account.setNumber("123456789012");
        account.setOwnerId(1L);
        account.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void testGetAccount_Success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account result = accountService.getAccount(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getId());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testGetAccount_NotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> accountService.getAccount(accountId));

        assertEquals("Account not found with id: " + accountId, exception.getMessage());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testCreateAccount() {
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(account);

        assertNotNull(result);
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        verify(freeAccountNumberService, times(1)).assignAccountNumber(account);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testBlockAccount() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.blockAccount(accountId);

        assertEquals(AccountStatus.FROZEN, result.getStatus());
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testCloseAccount() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.closeAccount(accountId);

        assertEquals(AccountStatus.CLOSED, result.getStatus());
        assertNotNull(result.getClosedAt());
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).save(account);
    }
}