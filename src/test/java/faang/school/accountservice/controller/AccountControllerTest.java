package faang.school.accountservice.controller;

import faang.school.accountservice.config.context.OwnershipChecker;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.mapper.AccountMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long USER_ID = 7L;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserContext userContext;

    @Mock
    private OwnershipChecker ownershipChecker;

    @InjectMocks
    private AccountController accountController;

    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(ACCOUNT_ID);
        account.setOwnerId(USER_ID);
        account.setStatus(AccountStatus.ACTIVE);

        accountDto = AccountDto.builder()
                .id(ACCOUNT_ID)
                .ownerId(USER_ID)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void getAccount_whenOwnerHasAccess_returnsMappedDto() {
        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        ResponseEntity<AccountDto> response = accountController.getAccount(ACCOUNT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(accountDto);
        verify(ownershipChecker).assertCanAccess(account);
    }

    @Test
    void getAccount_whenOwnershipDenied_propagatesSecurityException() {
        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(account);
        org.mockito.Mockito.doThrow(new SecurityException("denied"))
                .when(ownershipChecker).assertCanAccess(account);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> accountController.getAccount(ACCOUNT_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("denied");
    }

    @Test
    void createAccount_whenUserMatchesOwner_createsAndReturnsDto() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        Account incoming = new Account();
        when(accountMapper.toEntity(accountDto)).thenReturn(incoming);
        when(accountService.createAccount(incoming)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        ResponseEntity<AccountDto> response = accountController.createAccount(accountDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(accountDto);
        verify(accountService).createAccount(incoming);
    }

    @Test
    void createAccount_whenUserNotAuthenticated_returnsForbidden() {
        when(userContext.getUserId()).thenReturn(null);

        ResponseEntity<AccountDto> response = accountController.createAccount(accountDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNull();
        verify(accountService, never()).createAccount(any());
    }

    @Test
    void createAccount_whenUserDoesNotMatchOwner_returnsForbidden() {
        when(userContext.getUserId()).thenReturn(99L);

        ResponseEntity<AccountDto> response = accountController.createAccount(accountDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(accountService, never()).createAccount(any());
    }

    @Test
    void blockAccount_whenOwnerHasAccess_blocksAndReturnsDto() {
        Account blocked = new Account();
        blocked.setStatus(AccountStatus.FROZEN);
        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(account);
        when(accountService.blockAccount(ACCOUNT_ID)).thenReturn(blocked);
        when(accountMapper.toDto(blocked)).thenReturn(accountDto);

        ResponseEntity<AccountDto> response = accountController.blockAccount(ACCOUNT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ownershipChecker).assertCanAccess(account);
        verify(accountService).blockAccount(ACCOUNT_ID);
    }

    @Test
    void closeAccount_whenOwnerHasAccess_closesAndReturnsDto() {
        Account closed = new Account();
        closed.setStatus(AccountStatus.CLOSED);
        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(account);
        when(accountService.closeAccount(ACCOUNT_ID)).thenReturn(closed);
        when(accountMapper.toDto(closed)).thenReturn(accountDto);

        ResponseEntity<AccountDto> response = accountController.closeAccount(ACCOUNT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ownershipChecker).assertCanAccess(account);
        verify(accountService).closeAccount(ACCOUNT_ID);
    }
}
