package faang.school.accountservice.service;

import faang.school.accountservice.config.account.AccountProperties;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.FreeAccountId;
import faang.school.accountservice.model.FreeAccountNumber;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.AccountSeqRepository;
import faang.school.accountservice.repository.FreeAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeAccountNumberServiceTest {

    @Mock
    private AccountSeqRepository accountSeqRepository;

    @Mock
    private FreeAccountRepository freeAccountRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountProperties accountProperties;

    private FreeAccountNumberService freeAccountNumberService;
    private Account account;
    private FreeAccountNumber freeAccountNumber;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(accountProperties.getBaseNumber(anyString())).thenReturn("4200000000000000");

        freeAccountNumberService = spy(new FreeAccountNumberService(accountSeqRepository, freeAccountRepository, accountRepository, accountProperties));

        account = Account.builder().id(1L).ownerId(1L).type(AccountType.DEBIT).currency(Currency.RUB).status(AccountStatus.ACTIVE).build();
        freeAccountNumber = new FreeAccountNumber(new FreeAccountId(AccountType.DEBIT, 4200000000000001L));
    }

    @Test
    void testAssignAccountNumber_Success() {
        when(freeAccountRepository.retrieveFirst(AccountType.DEBIT.name())).thenReturn(Optional.of(freeAccountNumber));

        freeAccountNumberService.assignAccountNumber(account);

        assertEquals("4200000000000001", account.getNumber());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testGenerateMissingAccountNumbers_MissingNumbersExist() {
        AccountType testAccountType = AccountType.DEBIT;
        when(freeAccountRepository.countByType(testAccountType)).thenReturn(1);

        freeAccountNumberService.generateMissingAccountNumbers(testAccountType, 5);

        verify(freeAccountRepository, times(1)).countByType(testAccountType);
        verify(accountSeqRepository, times(1)).incrementCounter(testAccountType.name(), 4);
    }

    @Test
    void testGenerateMissingAccountNumbers_NoMissingNumbers() {
        AccountType testAccountType = AccountType.DEBIT;
        when(freeAccountRepository.countByType(testAccountType)).thenReturn(10);

        freeAccountNumberService.generateMissingAccountNumbers(testAccountType, 10);

        verify(freeAccountRepository, times(1)).countByType(testAccountType);
        verify(accountSeqRepository, never()).incrementCounter(anyString(), anyInt());
    }
}