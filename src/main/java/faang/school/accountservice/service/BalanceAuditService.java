package faang.school.accountservice.service;

import faang.school.accountservice.enums.BalanceAuditOutcome;
import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.model.BalanceAudit;
import faang.school.accountservice.repository.BalanceAuditRepository;
import faang.school.accountservice.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BalanceAuditService {

    private final BalanceAuditRepository balanceAuditRepository;
    private final BalanceRepository balanceRepository;
    private final BalanceMapper balanceMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BalanceAudit createSuccessfulAudit(Balance balance, UUID transactionId, String operation) {
        return createSuccessfulAudit(balance, transactionId, operation, "IMMEDIATE");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BalanceAudit createSuccessfulAudit(Balance balance, UUID transactionId, String operation,
                                              String commitStatus) {
        BalanceAudit audit = balanceMapper.toBalanceAudit(balance);
        audit.setTransactionId(transactionId);
        audit.setOperation(operation);
        audit.setOutcome(BalanceAuditOutcome.SUCCESS);
        audit.setCommitStatus(commitStatus);
        BalanceAudit saved = balanceAuditRepository.save(audit);
        log.info("Audited successful balance action: {}", saved);
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BalanceAudit createFailedAudit(long accountId,
                                          UUID transactionId,
                                          String operation,
                                          String failureReason) {
        Balance balance = balanceRepository.findByAccountId(accountId);
        if (balance == null) {
            log.warn("Cannot create balance audit because account {} has no balance", accountId);
            return null;
        }

        BalanceAudit audit = balanceMapper.toBalanceAudit(balance);
        audit.setTransactionId(transactionId);
        audit.setOperation(operation);
        audit.setOutcome(BalanceAuditOutcome.FAILED);
        audit.setFailureReason(truncate(failureReason));
        audit.setCommitStatus("IMMEDIATE");
        BalanceAudit saved = balanceAuditRepository.save(audit);
        log.info("Audited failed balance action: {}", saved);
        return saved;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 255) {
            return value;
        }
        return value.substring(0, 255);
    }
}
