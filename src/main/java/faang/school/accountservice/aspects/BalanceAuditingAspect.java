package faang.school.accountservice.aspects;

import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceAuditService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class BalanceAuditingAspect {

    private final BalanceAuditService balanceAuditService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Pointcut("@annotation(faang.school.accountservice.annotations.Auditable)")
    public void getAuditableMethods() {
    }

    @AfterThrowing(value = "getAuditableMethods()", throwing = "ex")
    public void afterFailedOperation(JoinPoint joinPoint, IllegalStateException ex) {
        log.info("Balance operation is failed in method: {}. Exception is thrown: {}", joinPoint.toString(), ex.toString());

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        int index = Arrays.asList(codeSignature.getParameterNames()).indexOf("accountId");
        long id = (long) joinPoint.getArgs()[index];

        balanceAuditService.createFailedAudit(id);
    }

    @AfterReturning(value = "getAuditableMethods()", returning = "result")
    public void afterSuccessfulOperation(Balance result) {
        if (result != null) {
            entityManager.flush();
            entityManager.refresh(result);
            balanceAuditService.createSuccessfulAudit(result);
        }
    }
}
