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
    public Balance createBalance(Balance balance) {
        Long accountId = balance.getAccount().getId();
        if (balanceRepository.existsByAccountId(accountId)) {
            throw new IllegalStateException("Balance for this account already exists.");
        }

        Account account = accountService.getAccount(accountId);
        balance.setAccount(account);

        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance updateBalance(Balance newBalance) {
        Long accountId = newBalance.getAccount().getId();
        Balance actualBalance = balanceRepository.findByAccountId(accountId);

        actualBalance.setAuthorizedBalance(newBalance.getAuthorizedBalance());
        actualBalance.setActualBalance(newBalance.getActualBalance());

        return balanceRepository.save(actualBalance);
    }

    @Transactional
    public Balance authorizePayment(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        if (balance.getActualBalance().subtract(balance.getAuthorizedBalance()).compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds for authorization.");
        }

        balance.setAuthorizedBalance(balance.getAuthorizedBalance().add(amount));
        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance capturePayment(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        if (balance.getAuthorizedBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough authorized balance.");
        }

        balance.setAuthorizedBalance(balance.getAuthorizedBalance().subtract(amount));
        balance.setActualBalance(balance.getActualBalance().subtract(amount));
        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance cancelAuthorization(long accountId, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountId(accountId);

        if (balance.getAuthorizedBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough authorized balance to cancel.");
        }

        balance.setAuthorizedBalance(balance.getAuthorizedBalance().subtract(amount));
        return balanceRepository.save(balance);
    }
}
