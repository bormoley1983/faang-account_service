package faang.school.accountservice.service;

import faang.school.accountservice.annotations.Auditable;
import faang.school.accountservice.exeption.BalanceNotFoundException;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Service
public class BalanceService {
    private final BalanceRepository balanceRepository;
    private final AccountService accountService;

    @Transactional(readOnly = true)
    public Balance getBalanceByAccountId(long accountId) {
        return getRequiredBalance(accountId);
    }

    @Auditable
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

    @Auditable
    @Transactional
    public Balance authorizeAmount(long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        Balance balance = getRequiredBalance(accountId);

        BigDecimal currentActualBalance = balance.getActualBalance().subtract(balance.getAuthorizedBalance());
        if(currentActualBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        BigDecimal newAuthorizedBalance = balance.getAuthorizedBalance().add(amount);
        balance.setAuthorizedBalance(newAuthorizedBalance);

        return balanceRepository.save(balance);
    }

    @Auditable
    @Transactional
    public Balance commitAuthorization(long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        Balance balance = getRequiredBalance(accountId);
        validateAuthorizedAmount(balance, amount);

        BigDecimal newAuthorizedBalance = balance.getAuthorizedBalance().subtract(amount);
        BigDecimal newActualBalance = balance.getActualBalance().subtract(amount);
        balance.setAuthorizedBalance(newAuthorizedBalance);
        balance.setActualBalance(newActualBalance);

        return balanceRepository.save(balance);
    }

    @Auditable
    @Transactional
    public Balance cancelAuthorization(long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        Balance balance = getRequiredBalance(accountId);
        validateAuthorizedAmount(balance, amount);

        BigDecimal newAuthorizedBalance = balance.getAuthorizedBalance().subtract(amount);
        balance.setAuthorizedBalance(newAuthorizedBalance);

        return balanceRepository.save(balance);
    }

    @Auditable
    @Transactional
    public Balance creditBalance(long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        Balance balance = getRequiredBalance(accountId);

        BigDecimal newActualBalance = balance.getActualBalance().add(amount);
        balance.setActualBalance(newActualBalance);

        return balanceRepository.save(balance);
    }

    @Auditable
    @Transactional
    public Balance debitBalance(long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        Balance balance = getRequiredBalance(accountId);

        BigDecimal currentActualBalance = balance.getActualBalance().subtract(balance.getAuthorizedBalance());
        if(currentActualBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        BigDecimal newActualBalance = balance.getActualBalance().subtract(amount);
        balance.setActualBalance(newActualBalance);

        return balanceRepository.save(balance);
    }

    private Balance getRequiredBalance(long accountId) {
        Balance balance = balanceRepository.findByAccountId(accountId);
        if (balance == null) {
            throw new BalanceNotFoundException(accountId);
        }
        return balance;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private void validateAuthorizedAmount(Balance balance, BigDecimal amount) {
        if (balance.getAuthorizedBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient authorized funds");
        }
    }
}
