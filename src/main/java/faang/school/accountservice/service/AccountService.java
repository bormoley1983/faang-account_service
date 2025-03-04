package faang.school.accountservice.service;

import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.enums.Status;
import faang.school.accountservice.mapper.AccountMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountDto getAccount(Long accountId) {
        Account account = getAccountById(accountId);
        return accountMapper.toDto(account);
    }

    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            return List.of();
        }
        return accounts.stream()
                .map(accountMapper::toDto).toList();
    }

    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = accountMapper.toEntity(accountDto);
        account.setStatus(Status.ACTIVE);
        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Transactional
    public AccountDto activateAccount(Long accountId) {
        Account account = getAccountById(accountId);
        account.setStatus(Status.ACTIVE);
        account.setVersion(account.getVersion() + 1);
        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Transactional
    public AccountDto blockAccount(Long accountId) {
        Account account = getAccountById(accountId);
        account.setStatus(Status.FROZEN);
        account.setVersion(account.getVersion() + 1);
        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Transactional
    public AccountDto closeAccount(Long accountId) {
        Account account = getAccountById(accountId);
        account.setStatus(Status.CLOSED);
        account.setVersion(account.getVersion() + 1);
        account.setClosedAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    private Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Аккаунт с id %d не найден", accountId)));
    }
}
