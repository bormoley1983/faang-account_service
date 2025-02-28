package faang.school.accountservice.service;

import faang.school.accountservice.model.Balance;
import faang.school.accountservice.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BalanceService {
    private final BalanceRepository balanceRepository;

    @Transactional
    public Balance getBalanceByAccountId(long accountId) {
        return balanceRepository.findByAccountId(accountId);
    }
}
