package faang.school.accountservice.scheduler;

import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.service.InterestAccrualService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InterestAccrualSchedulerTest {

    @Mock
    private InterestAccrualService interestAccrualService;

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    private InterestAccrualScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new InterestAccrualScheduler(interestAccrualService, savingsAccountRepository);
    }

    @Test
    void schedulerPassesOnlyIdsToManagedAsyncWorkersAndWaitsForCompletion() {
        when(savingsAccountRepository.findAllAccountIdsRequiringInterest(any(LocalDate.class)))
                .thenReturn(List.of(1L, 2L));
        when(interestAccrualService.accrueInterestForAccount(any(Long.class), any(LocalDate.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        scheduler.scheduledAccrual();

        verify(interestAccrualService).accrueInterestForAccount(org.mockito.ArgumentMatchers.eq(1L), any(LocalDate.class));
        verify(interestAccrualService).accrueInterestForAccount(org.mockito.ArgumentMatchers.eq(2L), any(LocalDate.class));
    }

    @Test
    void schedulerDoesNotReportSuccessWhenAWorkerFails() {
        CompletableFuture<Void> failed = CompletableFuture.failedFuture(new IllegalStateException("accrual failed"));
        when(savingsAccountRepository.findAllAccountIdsRequiringInterest(any(LocalDate.class)))
                .thenReturn(List.of(1L));
        when(interestAccrualService.accrueInterestForAccount(any(Long.class), any(LocalDate.class)))
                .thenReturn(failed);

        assertThrows(CompletionException.class, scheduler::scheduledAccrual);
    }
}
