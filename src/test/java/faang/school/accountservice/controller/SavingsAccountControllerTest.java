package faang.school.accountservice.controller;

import faang.school.accountservice.config.context.OwnershipChecker;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.dto.savingsAccount.AmountDto;
import faang.school.accountservice.dto.savingsAccount.SavingsAccountDto;
import faang.school.accountservice.dto.savingsAccount.SavingsAccountRegisterDto;
import faang.school.accountservice.mapper.SavingsAccountMapper;
import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.service.SavingsAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavingsAccountControllerTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long OWNER_ID = 7L;
    private static final Long TARIFF_ID = 3L;
    private static final BigDecimal AMOUNT = new BigDecimal("50.00");

    @Mock
    private SavingsAccountMapper savingsAccountMapper;

    @Mock
    private SavingsAccountService savingsAccountService;

    @Mock
    private UserContext userContext;

    @Mock
    private OwnershipChecker ownershipChecker;

    @InjectMocks
    private SavingsAccountController savingsAccountController;

    private SavingsAccount savingsAccount;
    private SavingsAccountDto savingsAccountDto;

    @BeforeEach
    void setUp() {
        savingsAccount = new SavingsAccount();
        savingsAccount.setBalance(BigDecimal.ZERO);

        savingsAccountDto = SavingsAccountDto.builder()
                .id(ACCOUNT_ID)
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void openSavingsAccount_whenAccessAllowed_delegatesWithTariffId() {
        SavingsAccountRegisterDto registerDto = new SavingsAccountRegisterDto(TARIFF_ID);
        when(savingsAccountService.openSavingsAccount(ACCOUNT_ID, TARIFF_ID)).thenReturn(savingsAccount);
        when(savingsAccountMapper.toDto(savingsAccount)).thenReturn(savingsAccountDto);

        ResponseEntity<SavingsAccountDto> response = savingsAccountController.openSavingsAccount(ACCOUNT_ID, registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ownershipChecker).assertCanAccess(ACCOUNT_ID);
        verify(savingsAccountService).openSavingsAccount(ACCOUNT_ID, TARIFF_ID);
    }

    @Test
    void getSavingsAccount_whenAccessAllowed_returnsMappedDto() {
        when(savingsAccountService.getSavingsAccount(ACCOUNT_ID)).thenReturn(savingsAccount);
        when(savingsAccountMapper.toDto(savingsAccount)).thenReturn(savingsAccountDto);

        ResponseEntity<SavingsAccountDto> response = savingsAccountController.getSavingsAccount(ACCOUNT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ownershipChecker).assertCanAccess(ACCOUNT_ID);
    }

    @Test
    void getSavingsAccountByOwnerId_whenUserMatchesOwner_returnsMappedDto() {
        when(userContext.getUserId()).thenReturn(OWNER_ID);
        when(savingsAccountService.getSavingsAccountByOwnerId(OWNER_ID)).thenReturn(savingsAccount);
        when(savingsAccountMapper.toDto(savingsAccount)).thenReturn(savingsAccountDto);

        ResponseEntity<SavingsAccountDto> response = savingsAccountController.getSavingsAccountByOwnerId(OWNER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(savingsAccountService).getSavingsAccountByOwnerId(OWNER_ID);
    }

    @Test
    void getSavingsAccountByOwnerId_whenUserNotAuthenticated_throwsSecurityException() {
        when(userContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> savingsAccountController.getSavingsAccountByOwnerId(OWNER_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Authenticated user is required and must match the requested owner");

        verify(savingsAccountService, never()).getSavingsAccountByOwnerId(OWNER_ID);
    }

    @Test
    void getSavingsAccountByOwnerId_whenUserDoesNotMatchOwner_throwsSecurityException() {
        when(userContext.getUserId()).thenReturn(99L);

        assertThatThrownBy(() -> savingsAccountController.getSavingsAccountByOwnerId(OWNER_ID))
                .isInstanceOf(SecurityException.class);

        verify(savingsAccountService, never()).getSavingsAccountByOwnerId(OWNER_ID);
    }

    @Test
    void deposit_whenAccessAllowed_delegatesWithAmount() {
        AmountDto amountDto = new AmountDto(AMOUNT);
        when(savingsAccountService.deposit(ACCOUNT_ID, AMOUNT)).thenReturn(savingsAccount);
        when(savingsAccountMapper.toDto(savingsAccount)).thenReturn(savingsAccountDto);

        ResponseEntity<SavingsAccountDto> response = savingsAccountController.deposit(ACCOUNT_ID, amountDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(savingsAccountService).deposit(ACCOUNT_ID, AMOUNT);
    }

    @Test
    void withdraw_whenAccessAllowed_delegatesWithAmount() {
        AmountDto amountDto = new AmountDto(AMOUNT);
        when(savingsAccountService.withdraw(ACCOUNT_ID, AMOUNT)).thenReturn(savingsAccount);
        when(savingsAccountMapper.toDto(savingsAccount)).thenReturn(savingsAccountDto);

        ResponseEntity<SavingsAccountDto> response = savingsAccountController.withdraw(ACCOUNT_ID, amountDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(savingsAccountService).withdraw(ACCOUNT_ID, AMOUNT);
    }
}
