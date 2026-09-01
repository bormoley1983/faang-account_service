package faang.school.accountservice.aspects;

import faang.school.accountservice.annotations.Auditable;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceAuditService;
import faang.school.accountservice.service.id.UuidV7Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Slf4j
@Aspect
@Order(0)
@RequiredArgsConstructor
@Component
public class BalanceAuditingAspect {

    private final BalanceAuditService balanceAuditService;
    private final UuidV7Generator uuidV7Generator;

    @Around("@annotation(auditable)")
    public Object auditBalanceOperation(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long accountId = getAccountId(joinPoint.getArgs(), auditable.accountIdArgument());
        UUID transactionId = uuidV7Generator.generate();
        String operation = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();

        try {
            Object result = joinPoint.proceed();
            if (result instanceof Balance balance) {
                persistSuccessfulAuditAfterCommit(balance, transactionId, operation);
            }
            return result;
        } catch (Throwable businessException) {
            try {
                balanceAuditService.createFailedAudit(
                        accountId,
                        transactionId,
                        operation,
                        businessException.getMessage()
                );
            } catch (RuntimeException auditException) {
                log.error("Failed to persist failed balance audit for transaction {}",
                        transactionId, auditException);
            }
            throw businessException;
        }
    }

    private long getAccountId(Object[] arguments, int accountIdArgument) {
        if (accountIdArgument < 0 || accountIdArgument >= arguments.length
                || !(arguments[accountIdArgument] instanceof Number accountId)) {
            throw new IllegalStateException("Auditable method must identify a numeric account ID argument");
        }
        return accountId.longValue();
    }

    private void persistSuccessfulAuditAfterCommit(Balance balance, UUID transactionId, String operation) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        balanceAuditService.createSuccessfulAudit(balance, transactionId, operation, "AFTER_COMMIT");
                    } catch (RuntimeException auditException) {
                        log.error("Failed to persist successful balance audit for transaction {}",
                                transactionId, auditException);
                    }
                }
            });
        } else {
            try {
                balanceAuditService.createSuccessfulAudit(balance, transactionId, operation, "IMMEDIATE");
            } catch (RuntimeException auditException) {
                log.error("Failed to persist successful balance audit for transaction {}",
                        transactionId, auditException);
            }
        }
    }
}
