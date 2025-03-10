package faang.school.accountservice.service;

import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.model.BalanceAudit;
import faang.school.accountservice.repository.BalanceAuditRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BalanceAuditService {

    private final BalanceAuditRepository balanceAuditRepository;
    private final BalanceService balanceService;
    private final BalanceMapper balanceMapper;

    @Transactional
    public BalanceAudit createSuccessfulAudit(Balance balance) {
        BalanceAudit audit = balanceMapper.toBalanceAudit(balance);
        BalanceAudit saved = balanceAuditRepository.save(audit);
        log.info("Audited successful balance action: {}", saved);
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BalanceAudit createFailedAudit(long accountId) {
        Balance balance = balanceService.getBalanceByAccountId(accountId);
        BalanceAudit audit = balanceMapper.toBalanceAudit(balance);
        BalanceAudit saved = balanceAuditRepository.save(audit);
        log.info("Audited failed balance action: {}", saved);
        return saved;
    }
    
}
