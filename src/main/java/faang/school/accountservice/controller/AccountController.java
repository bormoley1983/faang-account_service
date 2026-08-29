package faang.school.accountservice.controller;

import faang.school.accountservice.config.context.OwnershipChecker;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.mapper.AccountMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/accounts")
@RestController
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final UserContext userContext;
    private final OwnershipChecker ownershipChecker;

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
        Account account = accountService.getAccount(id);
        ownershipChecker.assertCanAccess(account);
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto) {
        if (userContext.getUserId() == null || !userContext.getUserId().equals(accountDto.ownerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Account account = accountMapper.toEntity(accountDto);
        Account savedAccount = accountService.createAccount(account);
        return ResponseEntity.ok(accountMapper.toDto(savedAccount));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<AccountDto> blockAccount(@PathVariable Long id) {
        Account account = accountService.getAccount(id);
        ownershipChecker.assertCanAccess(account);
        return ResponseEntity.ok(accountMapper.toDto(accountService.blockAccount(id)));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AccountDto> closeAccount(@PathVariable Long id) {
        Account account = accountService.getAccount(id);
        ownershipChecker.assertCanAccess(account);
        return ResponseEntity.ok(accountMapper.toDto(accountService.closeAccount(id)));
    }
}