package faang.school.accountservice.aspects;

import faang.school.accountservice.annotations.Auditable;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceAuditService;
import faang.school.accountservice.service.BalanceService;
import faang.school.accountservice.service.id.UuidV7Generator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BalanceAuditingAspectTest {

    private static final UUID TRANSACTION_ID = UUID.fromString("019535d9-3df7-79fb-b466-fa907fa17f9e");

    @Mock
    private BalanceAuditService balanceAuditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Auditable auditable;

    @Mock
    private UuidV7Generator uuidV7Generator;

    private BalanceAuditingAspect aspect;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        MockitoAnnotations.openMocks(this);
        aspect = new BalanceAuditingAspect(balanceAuditService, uuidV7Generator);
        when(uuidV7Generator.generate()).thenReturn(TRANSACTION_ID);
        when(auditable.accountIdArgument()).thenReturn(0);
        when(joinPoint.getArgs()).thenReturn(new Object[]{42L, BigDecimal.ONE});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                BalanceService.class.getMethod("creditBalance", long.class, BigDecimal.class)
        );
    }

    @Test
    void successfulOperationGetsUuidAndMethodMetadataWithoutParameterNameReflection() throws Throwable {
        Balance balance = Balance.builder().id(5L).build();
        when(joinPoint.proceed()).thenReturn(balance);

        Object result = aspect.auditBalanceOperation(joinPoint, auditable);

        assertSame(balance, result);
        verify(balanceAuditService).createSuccessfulAudit(
                org.mockito.ArgumentMatchers.same(balance),
                org.mockito.ArgumentMatchers.eq(TRANSACTION_ID),
                org.mockito.ArgumentMatchers.eq("creditBalance")
        );
    }

    @Test
    void failedOperationIsAuditedAndOriginalExceptionIsPreserved() throws Throwable {
        IllegalStateException failure = new IllegalStateException("insufficient funds");
        when(joinPoint.proceed()).thenThrow(failure);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> aspect.auditBalanceOperation(joinPoint, auditable)
        );

        assertSame(failure, thrown);
        verify(balanceAuditService).createFailedAudit(
                org.mockito.ArgumentMatchers.eq(42L),
                org.mockito.ArgumentMatchers.eq(TRANSACTION_ID),
                org.mockito.ArgumentMatchers.eq("creditBalance"),
                org.mockito.ArgumentMatchers.eq("insufficient funds")
        );
    }

    @AfterEach
    void cleanupTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void successfulOperationWithinTransaction_defersAuditToAfterCommit() throws Throwable {
        Balance balance = Balance.builder().id(5L).build();
        when(joinPoint.proceed()).thenReturn(balance);
        TransactionSynchronizationManager.initSynchronization();

        try {
            Object result = aspect.auditBalanceOperation(joinPoint, auditable);

            assertSame(balance, result);
            // Audit must NOT be written synchronously while the transaction is active.
            verify(balanceAuditService, never()).createSuccessfulAudit(any(), any(), any());

            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager.getSynchronizations();
            assertThat(synchronizations).hasSize(1);

            // Simulate commit: the registered synchronization fires the audit.
            for (TransactionSynchronization synchronization : synchronizations) {
                synchronization.afterCommit();
            }
            verify(balanceAuditService).createSuccessfulAudit(
                    org.mockito.ArgumentMatchers.same(balance),
                    org.mockito.ArgumentMatchers.eq(TRANSACTION_ID),
                    org.mockito.ArgumentMatchers.eq("creditBalance")
            );
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void successfulOperation_auditFailureIsSwallowed() throws Throwable {
        Balance balance = Balance.builder().id(5L).build();
        when(joinPoint.proceed()).thenReturn(balance);
        doThrow(new RuntimeException("audit db down"))
                .when(balanceAuditService).createSuccessfulAudit(any(), any(), any());

        Object result = aspect.auditBalanceOperation(joinPoint, auditable);

        assertSame(balance, result);
    }

    @Test
    void failedOperation_auditFailureIsSwallowedAndOriginalExceptionPreserved() throws Throwable {
        IllegalStateException failure = new IllegalStateException("insufficient funds");
        when(joinPoint.proceed()).thenThrow(failure);
        doThrow(new RuntimeException("audit db down"))
                .when(balanceAuditService).createFailedAudit(anyLong(), any(), any(), any());

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> aspect.auditBalanceOperation(joinPoint, auditable)
        );

        assertSame(failure, thrown);
    }

    @Test
    void invalidAccountIdArgument_outOfRange_throwsIllegalState() throws Throwable {
        when(auditable.accountIdArgument()).thenReturn(5);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> aspect.auditBalanceOperation(joinPoint, auditable)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Auditable method must identify a numeric account ID argument");
    }

    @Test
    void invalidAccountIdArgument_nonNumber_throwsIllegalState() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not-a-number"});

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> aspect.auditBalanceOperation(joinPoint, auditable)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Auditable method must identify a numeric account ID argument");
    }
}
