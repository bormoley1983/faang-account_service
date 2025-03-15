package faang.school.accountservice.service;

import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final FreeAccountNumberService freeAccountNumberService;

    public Account getAccount(Long id) {
        return getAccountById(id);
    }

    @Transactional
    public Account createAccount(Account account) {
        account.setStatus(AccountStatus.ACTIVE);
        freeAccountNumberService.assignAccountNumber(account);
        return accountRepository.save(account);
    }

    @Transactional
    public Account blockAccount(Long id) {
        return updateAccountStatus(id, AccountStatus.FROZEN, null);
    }

    @Transactional
    public Account closeAccount(Long id) {
        return updateAccountStatus(id, AccountStatus.CLOSED, LocalDateTime.now());
    }

    private Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + id));
    }

    private Account updateAccountStatus(Long id, AccountStatus status, LocalDateTime closedAt) {
        Account account = getAccountById(id);
        account.setStatus(status);

        Optional.ofNullable(closedAt).ifPresent(account::setClosedAt);

        return accountRepository.save(account);
    }
}