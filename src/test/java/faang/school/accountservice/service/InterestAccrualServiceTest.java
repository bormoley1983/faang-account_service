package faang.school.accountservice.service;

import faang.school.accountservice.dto.savingsAccount.TariffHistorySnapshot;
import faang.school.accountservice.dto.savingsAccount.TariffSnapshot;
import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InterestAccrualServiceTest {

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private InterestAccrualService interestAccrualService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void accrualUsesEveryElapsedDayAndPersistsItInTheWorker() {
        long accountId = 7L;
        long tariffId = 3L;
        LocalDate today = LocalDate.of(2026, 8, 29);
        Tariff tariff = Tariff.builder()
                .id(tariffId)
                .rateHistory(List.of(new BigDecimal("36.50")))
                .build();
        TariffSnapshot tariffSnapshot = TariffSnapshot.builder()
                .id(tariffId)
                .build();
        SavingsAccount account = SavingsAccount.builder()
                .id(accountId)
                .balance(new BigDecimal("1000.00"))
                .lastInterestDate(today.minusDays(3))
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .tariffHistory(List.of(new TariffHistorySnapshot(
                        tariffSnapshot,
                        today.minusMonths(1),
                        null
                )))
                .build();
        when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));

        CompletableFuture<Void> result = interestAccrualService.accrueInterestForAccount(accountId, today);

        result.join();
        assertEquals(today, account.getLastInterestDate());
        assertEquals(0, new BigDecimal("1003.00").compareTo(account.getBalance()));
        verify(savingsAccountRepository).findById(accountId);
        verify(savingsAccountRepository).save(account);
    }

    @Test
    void accrualFailsWhenWorkerCannotReloadAccount() {
        long accountId = 7L;
        LocalDate today = LocalDate.of(2026, 8, 29);
        when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // accrual now runs synchronously on the caller thread, so the exception
        // propagates directly instead of being wrapped in a CompletionException.
        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> interestAccrualService.accrueInterestForAccount(accountId, today)
        );

        verify(savingsAccountRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void accrualSingleDayBoundary_countsExactlyOneDay() {
        long accountId = 10L;
        long tariffId = 5L;
        LocalDate today = LocalDate.of(2026, 8, 27);
        // 36.5% annual rate → daily rate = 36.5/100/365 = 0.001
        Tariff tariff = Tariff.builder()
                .id(tariffId)
                .rateHistory(List.of(new BigDecimal("36.50")))
                .build();
        TariffSnapshot tariffSnapshot = TariffSnapshot.builder()
                .id(tariffId)
                .build();
        SavingsAccount account = SavingsAccount.builder()
                .id(accountId)
                .balance(new BigDecimal("1000.00"))
                .lastInterestDate(today.minusDays(1)) // Aug 26
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .tariffHistory(List.of(new TariffHistorySnapshot(
                        tariffSnapshot, today.minusMonths(1), null)))
                .build();
        when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));

        interestAccrualService.accrueInterestForAccount(accountId, today).join();

        // 1000 * (36.5/100/365) * 1 = 1000 * 0.001 * 1 = 1.00
        assertEquals(0, new BigDecimal("1001.00").compareTo(account.getBalance()));
        assertEquals(today, account.getLastInterestDate());
    }

    @Test
    void accrualSameDayAsLastCalculation_noInterestAccrued() {
        long accountId = 11L;
        LocalDate today = LocalDate.of(2026, 8, 29);
        SavingsAccount account = SavingsAccount.builder()
                .id(accountId)
                .balance(new BigDecimal("500.00"))
                .lastInterestDate(today) // same day
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .tariffHistory(List.of())
                .build();
        when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        interestAccrualService.accrueInterestForAccount(accountId, today).join();

        assertEquals(0, new BigDecimal("500.00").compareTo(account.getBalance()));
        verify(savingsAccountRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void accrualNoLastInterestDate_usesCreatedAtAsStart() {
        long accountId = 12L;
        long tariffId = 6L;
        LocalDate today = LocalDate.of(2026, 8, 29);
        Tariff tariff = Tariff.builder()
                .id(tariffId)
                .rateHistory(List.of(new BigDecimal("36.50")))
                .build();
        TariffSnapshot tariffSnapshot = TariffSnapshot.builder()
                .id(tariffId)
                .build();
        SavingsAccount account = SavingsAccount.builder()
                .id(accountId)
                .balance(new BigDecimal("1000.00"))
                .lastInterestDate(null)
                .createdAt(LocalDateTime.of(2026, 8, 27, 12, 0)) // Aug 27
                .tariffHistory(List.of(new TariffHistorySnapshot(
                        tariffSnapshot, today.minusMonths(1), null)))
                .build();
        when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));

        interestAccrualService.accrueInterestForAccount(accountId, today).join();

        // DAYS.between(Aug 27, Aug 29) = 2 days
        // 1000 * (36.5/100/365) * 2 = 1000 * 0.001 * 2 = 2.00
        assertEquals(0, new BigDecimal("1002.00").compareTo(account.getBalance()));
        assertEquals(today, account.getLastInterestDate());
    }

    @Test
    void accrualZeroBalance_noInterestEvenWithElapsedDays() {
        long accountId = 13L;
        LocalDate today = LocalDate.of(2026, 8, 29);
        SavingsAccount account = SavingsAccount.builder()
                .id(accountId)
                .balance(BigDecimal.ZERO)
                .lastInterestDate(today.minusDays(10))
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .tariffHistory(List.of())
                .build();
        when(savingsAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        interestAccrualService.accrueInterestForAccount(accountId, today).join();

        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
        verify(savingsAccountRepository, never()).save(ArgumentMatchers.any());
    }
}
