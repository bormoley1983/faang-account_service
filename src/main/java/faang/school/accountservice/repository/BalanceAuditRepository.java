package faang.school.accountservice.repository;

import faang.school.accountservice.model.BalanceAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceAuditRepository extends JpaRepository<BalanceAudit, Long> {



}
