package faang.school.accountservice.service;

import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class BalanceService {
    private final BalanceRepository balanceRepository;
    private final AccountService accountService;

    @Transactional(readOnly = true)
    public Balance getBalanceByAccountId(long accountId) {
        return balanceRepository.findByAccountId(accountId);
    }

    @Transactional
    public Balance createBalance(long accountId) {
        if (balanceRepository.existsByAccountId(accountId)) {
            throw new IllegalStateException("Balance for this account already exists.");
        }

        Account account = accountService.getAccount(accountId);
        Balance balance = Balance.builder()
                .account(account)
                .build();

        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance authorizeAmount(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        BigDecimal currentActualBalance = balance.getActualBalance().subtract(balance.getAuthorizedBalance());
        if(currentActualBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        BigDecimal newAuthorizedBalance = balance.getAuthorizedBalance().add(amount);
        balance.setAuthorizedBalance(newAuthorizedBalance);

        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance commitAuthorization(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        BigDecimal newAuthorizedBalance = balance.getAuthorizedBalance().subtract(amount);
        BigDecimal newActualBalance = balance.getActualBalance().subtract(amount);
        balance.setAuthorizedBalance(newAuthorizedBalance);
        balance.setActualBalance(newActualBalance);

        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance cancelAuthorization(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        BigDecimal newAuthorizedBalance = balance.getAuthorizedBalance().subtract(amount);
        balance.setAuthorizedBalance(newAuthorizedBalance);

        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance creditBalance(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        BigDecimal newActualBalance = balance.getActualBalance().add(amount);
        balance.setActualBalance(newActualBalance);

        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance debitBalance(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        BigDecimal currentActualBalance = balance.getActualBalance().subtract(balance.getAuthorizedBalance());
        if(currentActualBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        BigDecimal newActualBalance = balance.getActualBalance().subtract(amount);
        balance.setActualBalance(newActualBalance);

        return balanceRepository.save(balance);
    }
}
