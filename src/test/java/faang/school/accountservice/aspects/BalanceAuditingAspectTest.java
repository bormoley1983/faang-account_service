package faang.school.accountservice.aspects;

import faang.school.accountservice.annotations.Auditable;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceAuditService;
import faang.school.accountservice.service.BalanceService;
import faang.school.accountservice.service.id.UuidV7Generator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
