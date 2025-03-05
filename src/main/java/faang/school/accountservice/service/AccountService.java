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
        return accountRepository.findAll().stream()
                .map(accountMapper::toDto)
                .toList();
    }

    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = accountMapper.toEntity(accountDto);
        return updateAccountStatusAndVersion(account, Status.ACTIVE);
    }

    @Transactional
    public AccountDto activateAccount(Long accountId) {
        Account account = getAccountById(accountId);
        return updateAccountStatusAndVersion(account, Status.ACTIVE);
    }

    @Transactional
    public AccountDto blockAccount(Long accountId) {
        Account account = getAccountById(accountId);
        return updateAccountStatusAndVersion(account, Status.FROZEN);
    }

    @Transactional
    public AccountDto closeAccount(Long accountId) {
        Account account = getAccountById(accountId);
        return updateAccountStatusAndVersion(account, Status.CLOSED);
    }

    private Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Аккаунт с id %d не найден", accountId)));
    }

    private AccountDto updateAccountStatusAndVersion(Account account, Status status) {
        validateStatusTransition(account.getStatus(), status);
        account.setStatus(status);
        account.setVersion(account.getVersion() + 1);
        if (status == Status.CLOSED) {
            account.setClosedAt(LocalDateTime.now());
        }
        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    private void validateStatusTransition(Status currentStatus, Status newStatus) {
        if (currentStatus == Status.CLOSED) {
            throw new IllegalStateException("Нельзя изменить статус закрытого аккаунта.");
        }
        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("Текущий статус совпадает с новым статусом.");
        }
    }
}
