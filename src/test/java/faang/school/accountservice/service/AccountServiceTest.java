package faang.school.accountservice.service;

import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.enums.Status;
import faang.school.accountservice.mapper.AccountMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private static final Long ACCOUNT_ID = 1L;
    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .number("123456789")
                .version(1)
                .build();
        accountDto = AccountDto.builder()
                .number("123456789")
                .build();
    }

    @Test
    void testGetAccountSuccess() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.getAccount(ACCOUNT_ID);

        assertEquals(accountDto, result);
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountMapper).toDto(account);
    }

    @Test
    void testGetAccountWithException() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> accountService.getAccount(ACCOUNT_ID));
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountMapper, never()).toDto(any());
    }

    @Test
    void testGetAllAccountsWithFullList() {
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        List<AccountDto> result = accountService.getAllAccounts();

        assertEquals(1, result.size());
        assertEquals(accountDto, result.get(0));
        verify(accountRepository).findAll();
        verify(accountMapper).toDto(account);
    }

    @Test
    void testGetAllAccountsWithEmptyList() {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        List<AccountDto> result = accountService.getAllAccounts();

        assertTrue(result.isEmpty());
        verify(accountRepository).findAll();
        verify(accountMapper, never()).toDto(any());
    }

    @Test
    void testCreateAccount() {
        when(accountMapper.toEntity(accountDto)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.createAccount(accountDto);

        assertEquals(accountDto, result);
        assertEquals(Status.ACTIVE, account.getStatus());
        assertEquals(2, account.getVersion());
        verify(accountMapper).toEntity(accountDto);
        verify(accountRepository).save(account);
        verify(accountMapper).toDto(account);
    }

    @Test
    void testActivateAccount() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.activateAccount(ACCOUNT_ID);

        assertEquals(accountDto, result);
        assertEquals(Status.ACTIVE, account.getStatus());
        assertEquals(2, account.getVersion());
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountRepository).save(account);
        verify(accountMapper).toDto(account);
    }

    @Test
    void testBlockAccount() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.blockAccount(ACCOUNT_ID);

        assertEquals(accountDto, result);
        assertEquals(Status.FROZEN, account.getStatus());
        assertEquals(2, account.getVersion());
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountRepository).save(account);
        verify(accountMapper).toDto(account);
    }

    @Test
    void testCloseAccount() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.closeAccount(ACCOUNT_ID);

        assertEquals(accountDto, result);
        assertEquals(Status.CLOSED, account.getStatus());
        assertEquals(2, account.getVersion());
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountRepository).save(account);
        verify(accountMapper).toDto(account);
    }

    @Test
    void testActivateAccountWhenAccountIsAlreadyActive() {
        account.setStatus(Status.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.activateAccount(ACCOUNT_ID));
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any());
        verify(accountMapper, never()).toDto(any());
    }

    @Test
    void testCloseAccountWhenAccountIsAlreadyClosed() {
        account.setStatus(Status.CLOSED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class,
                () -> accountService.closeAccount(ACCOUNT_ID));
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any());
        verify(accountMapper, never()).toDto(any());
    }
}