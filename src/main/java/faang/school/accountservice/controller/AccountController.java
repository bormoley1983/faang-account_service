package faang.school.accountservice.controller;

import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/{accountId}")
    public AccountDto getAccount(@PathVariable @NotNull @Positive Long accountId) {
        return accountService.getAccount(accountId);
    }

    @GetMapping
    public List<AccountDto> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto createAccount(@RequestBody @Valid AccountDto accountDto) {
        return accountService.createAccount(accountDto);
    }

    @PutMapping("/{accountId}/activateAccounts")
    public AccountDto activateAccount(@PathVariable @NotNull @Positive Long accountId) {
        return accountService.activateAccount(accountId);
    }

    @PutMapping("/{accountId}/blockAccounts")
    public AccountDto blockAccount(@PathVariable @NotNull @Positive Long accountId) {
        return accountService.blockAccount(accountId);
    }

    @PutMapping("/{accountId}/closeAccounts")
    public AccountDto closeAccount(@PathVariable @NotNull @Positive Long accountId) {
        return accountService.closeAccount(accountId);
    }
}
