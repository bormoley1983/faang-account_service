package faang.school.accountservice.service;

import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public Account getAccount(UUID id) {
        return getAccountById(id);
    }

    @Transactional
    public Account createAccount(Account account) {
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @Transactional
    public Account blockAccount(UUID id) {
        return updateAccountStatus(id, AccountStatus.FROZEN, null);
    }

    @Transactional
    public Account closeAccount(UUID id) {
        return updateAccountStatus(id, AccountStatus.CLOSED, LocalDateTime.now());
    }

    private Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private Account updateAccountStatus(UUID id, AccountStatus status, LocalDateTime closedAt) {
        Account account = getAccountById(id);
        account.setStatus(status);

        Optional.ofNullable(closedAt).ifPresent(account::setClosedAt);

        return accountRepository.save(account);
    }
}