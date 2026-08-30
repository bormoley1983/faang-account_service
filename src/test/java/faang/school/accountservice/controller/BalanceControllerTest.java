package faang.school.accountservice.controller;

import faang.school.accountservice.config.context.OwnershipChecker;
import faang.school.accountservice.dto.BalanceDto;
import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceControllerTest {

    private static final long ACCOUNT_ID = 1L;
    private static final BigDecimal AMOUNT = new BigDecimal("25.00");

    @Mock
    private BalanceService balanceService;

    @Mock
    private BalanceMapper balanceMapper;

    @Mock
    private OwnershipChecker ownershipChecker;

    @InjectMocks
    private BalanceController balanceController;

    private Balance balance;
    private BalanceDto balanceDto;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setId(ACCOUNT_ID);

        balance = new Balance();
        balance.setAccount(account);
        balance.setActualBalance(new BigDecimal("100.00"));
        balance.setAuthorizedBalance(BigDecimal.ZERO);

        balanceDto = new BalanceDto();
        balanceDto.setAccountId(ACCOUNT_ID);
    }

    @Test
    void getBalance_whenAccessAllowed_returnsMappedDto() {
        when(balanceService.getBalanceByAccountId(ACCOUNT_ID)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        ResponseEntity<BalanceDto> response = balanceController.getBalance(ACCOUNT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(balanceDto);
        verify(ownershipChecker).assertCanAccess(ACCOUNT_ID);
    }

    @Test
    void createBalance_whenAccessAllowed_delegatesToService() {
        when(balanceService.createBalance(ACCOUNT_ID)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        ResponseEntity<BalanceDto> response = balanceController.createBalance(ACCOUNT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ownershipChecker).assertCanAccess(ACCOUNT_ID);
        verify(balanceService).createBalance(ACCOUNT_ID);
    }

    @Test
    void authorizeAmount_whenAccessAllowed_delegatesToService() {
        when(balanceService.authorizeAmount(ACCOUNT_ID, AMOUNT)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        ResponseEntity<BalanceDto> response = balanceController.authorizeAmount(ACCOUNT_ID, AMOUNT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(balanceService).authorizeAmount(ACCOUNT_ID, AMOUNT);
    }

    @Test
    void commitAuthorization_whenAccessAllowed_delegatesToService() {
        when(balanceService.commitAuthorization(ACCOUNT_ID, AMOUNT)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        ResponseEntity<BalanceDto> response = balanceController.commitAuthorization(ACCOUNT_ID, AMOUNT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(balanceService).commitAuthorization(ACCOUNT_ID, AMOUNT);
    }

    @Test
    void cancelAuthorization_whenAccessAllowed_delegatesToService() {
        when(balanceService.cancelAuthorization(ACCOUNT_ID, AMOUNT)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        ResponseEntity<BalanceDto> response = balanceController.cancelAuthorization(ACCOUNT_ID, AMOUNT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(balanceService).cancelAuthorization(ACCOUNT_ID, AMOUNT);
    }

    @Test
    void creditBalance_whenAccessAllowed_delegatesToService() {
        when(balanceService.creditBalance(ACCOUNT_ID, AMOUNT)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        ResponseEntity<BalanceDto> response = balanceController.creditBalance(ACCOUNT_ID, AMOUNT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(balanceService).creditBalance(ACCOUNT_ID, AMOUNT);
    }

    @Test
    void debitBalance_whenAccessAllowed_delegatesToService() {
        when(balanceService.debitBalance(ACCOUNT_ID, AMOUNT)).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        ResponseEntity<BalanceDto> response = balanceController.debitBalance(ACCOUNT_ID, AMOUNT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(balanceService).debitBalance(ACCOUNT_ID, AMOUNT);
    }
}
