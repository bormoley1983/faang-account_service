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

@RequiredArgsConstructor
@RequestMapping("/accounts")
@RestController
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
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
    public ResponseEntity<AccountDto> blockAccount(@PathVariable Long id) {
        Account account = accountService.blockAccount(id);
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AccountDto> closeAccount(@PathVariable Long id) {
        Account account = accountService.closeAccount(id);
        return ResponseEntity.ok(accountMapper.toDto(account));
    }
}