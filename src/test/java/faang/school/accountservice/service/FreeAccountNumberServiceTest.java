package faang.school.accountservice.service;

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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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

    private FreeAccountNumberService freeAccountNumberService; // Убираем @Spy

    private Account account;
    private FreeAccountNumber freeAccountNumber;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        freeAccountNumberService = Mockito.spy(new FreeAccountNumberService(accountSeqRepository, freeAccountRepository, accountRepository));

        account = Account.builder()
                .id(1L)
                .ownerId(1L)
                .type(AccountType.DEBIT)
                .currency(Currency.RUB)
                .status(AccountStatus.ACTIVE)
                .build();

        freeAccountNumber = new FreeAccountNumber(new FreeAccountId(AccountType.DEBIT, 4200000000000001L));
    }

    @Test
    void testGenerateAccountNumbers_Success() {
        when(freeAccountRepository.countByType(AccountType.DEBIT)).thenReturn(5);
        when(accountSeqRepository.incrementCounter(AccountType.DEBIT.name(), 5))
                .thenReturn(Collections.singletonList(new Object[]{"DEBIT", 10L, 5L}));

        freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 5);

        verify(freeAccountRepository, times(5)).save(any(FreeAccountNumber.class));
    }

    @Test
    void testGenerateAccountNumbers_EnoughNumbers() {
        when(freeAccountRepository.countByType(AccountType.DEBIT)).thenReturn(15);

        freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 5);

        verify(freeAccountRepository, never()).save(any(FreeAccountNumber.class));
    }

    @Test
    void testAssignAccountNumber_Success() {
        when(freeAccountRepository.retrieveFirst(AccountType.DEBIT.name())).thenReturn(freeAccountNumber);

        freeAccountNumberService.assignAccountNumber(account);

        assertEquals("4200000000000001", account.getNumber());
        verify(accountRepository, times(1)).save(account);
    }
}