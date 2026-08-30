package faang.school.accountservice.service;

import faang.school.accountservice.config.account.AccountProperties;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.AccountSeq;
import faang.school.accountservice.model.FreeAccountId;
import faang.school.accountservice.model.FreeAccountNumber;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.AccountSeqRepository;
import faang.school.accountservice.repository.FreeAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
        ReflectionTestUtils.setField(freeAccountNumberService, "minFreeNumbers", 10);
        ReflectionTestUtils.setField(freeAccountNumberService, "generationBatchSize", 100);

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

    @Test
    void generateAccountNumbersPropagatesPersistenceFailure() {
        AccountSeq updatedSequence = new AccountSeq(AccountType.DEBIT, 2L);
        when(accountSeqRepository.incrementCounter(AccountType.DEBIT.name(), 2))
                .thenReturn(Optional.of(updatedSequence));
        when(freeAccountRepository.insertGeneratedBatch(
                AccountType.DEBIT.name(),
                4200000000000000L,
                0L,
                2L
        ))
                .thenThrow(new DataIntegrityViolationException("database rejected account number"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 2)
        );

        verify(freeAccountRepository, never()).countByType(AccountType.DEBIT);
    }

    @Test
    void ensureSufficientAccountNumbers_whenEnoughAvailable_doesNotGenerate() {
        when(freeAccountRepository.countByType(AccountType.DEBIT)).thenReturn(10);

        freeAccountNumberService.ensureSufficientAccountNumbers(AccountType.DEBIT);

        verify(accountSeqRepository, never()).incrementCounter(anyString(), anyInt());
    }

    @Test
    void ensureSufficientAccountNumbers_whenBelowMinimum_generatesBatch() {
        when(freeAccountRepository.countByType(AccountType.DEBIT)).thenReturn(5);
        AccountSeq updatedSequence = new AccountSeq(AccountType.DEBIT, 102L);
        when(accountSeqRepository.incrementCounter(AccountType.DEBIT.name(), 100))
                .thenReturn(Optional.of(updatedSequence));

        freeAccountNumberService.ensureSufficientAccountNumbers(AccountType.DEBIT);

        verify(freeAccountRepository).insertGeneratedBatch(
                AccountType.DEBIT.name(), 4200000000000000L, 2L, 102L);
    }

    @Test
    void generateAccountNumbers_whenPartialInsert_logsWarningAndContinues() {
        // Covers the insertedCount < requestedCount warning branch in processAccountNumbers.
        AccountSeq updatedSequence = new AccountSeq(AccountType.DEBIT, 5L);
        when(accountSeqRepository.incrementCounter(AccountType.DEBIT.name(), 2))
                .thenReturn(Optional.of(updatedSequence));
        when(freeAccountRepository.insertGeneratedBatch(
                AccountType.DEBIT.name(), 4200000000000000L, 3L, 5L)).thenReturn(1);
        when(freeAccountRepository.countByType(AccountType.DEBIT)).thenReturn(1);

        freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 2);

        verify(freeAccountRepository).insertGeneratedBatch(
                AccountType.DEBIT.name(), 4200000000000000L, 3L, 5L);
    }

    @Test
    void generateAccountNumbers_whenIncrementFails_doesNothing() {
        when(accountSeqRepository.incrementCounter(AccountType.DEBIT.name(), 5))
                .thenReturn(Optional.empty());

        freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 5);

        verify(freeAccountRepository, never()).insertGeneratedBatch(anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void generateAccountNumbers_whenBaseNumberInvalid_throwsIllegalState() {
        AccountSeq updatedSequence = new AccountSeq(AccountType.DEBIT, 5L);
        when(accountSeqRepository.incrementCounter(AccountType.DEBIT.name(), 2))
                .thenReturn(Optional.of(updatedSequence));
        when(accountProperties.getBaseNumber(anyString())).thenReturn("not-a-number");

        assertThrows(IllegalStateException.class,
                () -> freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 2));
    }

    @Test
    void generateAccountNumbersInsertsAllocatedRangeWithOneRepositoryCall() {
        AccountSeq updatedSequence = new AccountSeq(AccountType.DEBIT, 5L);
        when(accountSeqRepository.incrementCounter(AccountType.DEBIT.name(), 2))
                .thenReturn(Optional.of(updatedSequence));
        when(freeAccountRepository.insertGeneratedBatch(
                AccountType.DEBIT.name(),
                4200000000000000L,
                3L,
                5L
        )).thenReturn(2);

        freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 2);

        verify(freeAccountRepository).insertGeneratedBatch(
                AccountType.DEBIT.name(),
                4200000000000000L,
                3L,
                5L
        );
        verify(freeAccountRepository, never()).existsById(any());
        verify(freeAccountRepository, never()).save(any(FreeAccountNumber.class));
    }
}
