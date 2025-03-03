package faang.school.accountservice.service;

import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.model.BalanceAudit;
import faang.school.accountservice.repository.BalanceAuditRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BalanceListener {

    private final BalanceAuditRepository balanceAuditRepository;
    private final BalanceMapper balanceMapper;

    public BalanceListener(@Lazy BalanceAuditRepository balanceAuditRepository, BalanceMapper balanceMapper) {
        this.balanceAuditRepository = balanceAuditRepository;
        this.balanceMapper = balanceMapper;
    }

    @PostPersist
    @PostUpdate
    public void afterUpdate(Balance balance) {
        BalanceAudit audit = balanceMapper.toBalanceAudit(balance);
        audit.setTransactionId(1L);
        balanceAuditRepository.save(audit);
    }

}
