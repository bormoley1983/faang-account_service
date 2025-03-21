package faang.school.accountservice.controller;

import faang.school.accountservice.dto.savingsAccount.AmountDto;
import faang.school.accountservice.dto.savingsAccount.SavingsAccountDto;
import faang.school.accountservice.dto.savingsAccount.SavingsAccountRegisterDto;
import faang.school.accountservice.mapper.SavingsAccountMapper;
import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.service.SavingsAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/savingsAccount")
@RestController
public class SavingsAccountController {
    private final SavingsAccountMapper savingsAccountMapper;
    private final SavingsAccountService savingsAccountService;

    @PostMapping("/{accountId}")
    public ResponseEntity<SavingsAccountDto> openSavingsAccount(@PathVariable Long accountId,
                                                                @Valid @RequestBody SavingsAccountRegisterDto registerDto) {
        SavingsAccount savingsAccount = savingsAccountService.openSavingsAccount(accountId,
                registerDto.getTariffId());
        return ResponseEntity.ok(savingsAccountMapper.toDto(savingsAccount));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<SavingsAccountDto> getSavingsAccount(@PathVariable Long accountId) {
        SavingsAccount savingsAccount = savingsAccountService.getSavingsAccount(accountId);
        return ResponseEntity.ok(savingsAccountMapper.toDto(savingsAccount));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<SavingsAccountDto> getSavingsAccountByOwnerId(@PathVariable Long ownerId) {
        SavingsAccount savingsAccount = savingsAccountService.getSavingsAccountByOwnerId(ownerId);
        return ResponseEntity.ok(savingsAccountMapper.toDto(savingsAccount));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<SavingsAccountDto> deposit(@PathVariable Long accountId,
                                                     @Valid @RequestBody AmountDto amount) {
        SavingsAccount savingsAccount = savingsAccountService.deposit(accountId, amount.getAmount());
        return ResponseEntity.ok(savingsAccountMapper.toDto(savingsAccount));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<SavingsAccountDto> withdraw(@PathVariable Long accountId,
                                                      @Valid @RequestBody AmountDto amount) {
        SavingsAccount savingsAccount = savingsAccountService.withdraw(accountId, amount.getAmount());
        return ResponseEntity.ok(savingsAccountMapper.toDto(savingsAccount));
    }
}
