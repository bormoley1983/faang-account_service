package faang.school.accountservice.scheduler;

import faang.school.accountservice.config.context.ClusterLease;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.service.FreeAccountNumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountNumberSchedulerTest {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_FREE = 50;

    @Mock
    private FreeAccountNumberService freeAccountNumberService;
    @Mock
    private ClusterLease clusterLease;

    private AccountNumberScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new AccountNumberScheduler(freeAccountNumberService, clusterLease);
        ReflectionTestUtils.setField(scheduler, "batchSize", BATCH_SIZE);
        ReflectionTestUtils.setField(scheduler, "maxFreeNumbers", MAX_FREE);
    }

    @Test
    void generateAccounts_whenLeaseAcquired_generatesForEveryAccountType() {
        when(clusterLease.tryAcquire("account-number-generate")).thenReturn(true);

        scheduler.generateAccounts();

        for (AccountType type : AccountType.values()) {
            verify(freeAccountNumberService).generateAccountNumbers(eq(type), eq(BATCH_SIZE));
        }
        verify(clusterLease).release("account-number-generate");
    }

    @Test
    void generateAccounts_whenLeaseNotAcquired_skipsGeneration() {
        when(clusterLease.tryAcquire("account-number-generate")).thenReturn(false);

        scheduler.generateAccounts();

        for (AccountType type : AccountType.values()) {
            verify(freeAccountNumberService, never()).generateAccountNumbers(eq(type), anyInt());
        }
        verify(clusterLease, never()).release("account-number-generate");
    }

    @Test
    void generateAccounts_whenGenerationFails_stillReleasesLease() {
        when(clusterLease.tryAcquire("account-number-generate")).thenReturn(true);
        org.mockito.Mockito.doThrow(new RuntimeException("boom"))
                .when(freeAccountNumberService).generateAccountNumbers(eq(AccountType.values()[0]), eq(BATCH_SIZE));

        assertThatCode(scheduler::generateAccounts)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        verify(clusterLease).release("account-number-generate");
    }

    @Test
    void generateMissingAccounts_whenLeaseAcquired_generatesForEveryAccountType() {
        when(clusterLease.tryAcquire("account-number-missing")).thenReturn(true);

        scheduler.generateMissingAccounts();

        for (AccountType type : AccountType.values()) {
            verify(freeAccountNumberService).generateMissingAccountNumbers(eq(type), eq(MAX_FREE));
        }
        verify(clusterLease).release("account-number-missing");
    }

    @Test
    void generateMissingAccounts_whenLeaseNotAcquired_skipsGeneration() {
        when(clusterLease.tryAcquire("account-number-missing")).thenReturn(false);

        scheduler.generateMissingAccounts();

        for (AccountType type : AccountType.values()) {
            verify(freeAccountNumberService, never()).generateMissingAccountNumbers(eq(type), anyInt());
        }
    }
}
