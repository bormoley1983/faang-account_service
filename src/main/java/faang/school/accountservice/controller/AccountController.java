package faang.school.accountservice.controller;

import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.mapper.AccountMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/accounts")
@RestController
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper = AccountMapper.INSTANCE;

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable UUID id) {
        Account account = accountService.getAccount(id);
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto) {
        Account account = accountMapper.toEntity(accountDto);
        Account savedAccount = accountService.createAccount(account);
        return ResponseEntity.ok(accountMapper.toDto(savedAccount));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<AccountDto> blockAccount(@PathVariable UUID id) {
        Account blockedAccount = accountService.blockAccount(id);
        return ResponseEntity.ok(accountMapper.toDto(blockedAccount));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AccountDto> closeAccount(@PathVariable UUID id) {
        Account closedAccount = accountService.closeAccount(id);
        return ResponseEntity.ok(accountMapper.toDto(closedAccount));
    }
}