package faang.school.accountservice.service;

import faang.school.accountservice.dto.savingsAccount.TariffHistorySnapshot;
import faang.school.accountservice.dto.savingsAccount.TariffSnapshot;
import faang.school.accountservice.exeption.AccountNotFoundException;
import faang.school.accountservice.exeption.InsufficientFundsException;
import faang.school.accountservice.exeption.SavingsAccountNotFoundException;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SavingsAccountService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final AccountRepository accountRepository;
    private final TariffRepository tariffRepository;

    @Transactional
    public SavingsAccount openSavingsAccount(Long accountId, Long tariffId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new EntityNotFoundException("Tariff not found"));

        TariffSnapshot tariffSnapshot = buildTariffSnapshot(tariff);
        TariffHistorySnapshot historySnapshot = buildTariffHistorySnapshot(tariffSnapshot);

        SavingsAccount savingsAccount = SavingsAccount.builder()
                .balance(BigDecimal.ZERO)
                .account(account)
                .tariffHistory(List.of(historySnapshot))
                .build();

        return savingsAccountRepository.save(savingsAccount);
    }

    @Transactional(readOnly = true)
    public SavingsAccount getSavingsAccount(Long accountId) {
        return savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new SavingsAccountNotFoundException("SavingsAccount not found"));
    }

    @Transactional(readOnly = true)
    public SavingsAccount getSavingsAccountByOwnerId(Long ownerId) {
        return savingsAccountRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new SavingsAccountNotFoundException("SavingsAccount not found"));
    }

    @Transactional
    public SavingsAccount deposit(Long accountId, BigDecimal amount) {
        SavingsAccount account = savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        account.setBalance(account.getBalance().add(amount));
        return savingsAccountRepository.save(account);
    }

    @Transactional
    public SavingsAccount withdraw(Long accountId, BigDecimal amount) {
        SavingsAccount account = savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Not enough funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        return savingsAccountRepository.save(account);
    }

    private TariffSnapshot buildTariffSnapshot(Tariff tariff) {
        List<BigDecimal> rateHistory = tariff.getRateHistory();
        if (rateHistory == null || rateHistory.isEmpty()) {
            throw new IllegalStateException("Tariff has no rate history");
        }
        BigDecimal currentRate = rateHistory.get(rateHistory.size() - 1);

        return TariffSnapshot.builder()
                .id(tariff.getId())
                .name(tariff.getName())
                .rate(currentRate)
                .build();
    }

    private TariffHistorySnapshot buildTariffHistorySnapshot(TariffSnapshot snapshot) {
        return TariffHistorySnapshot.builder()
                .tariff(snapshot)
                .startDate(LocalDate.now())
                .build();
    }
}
