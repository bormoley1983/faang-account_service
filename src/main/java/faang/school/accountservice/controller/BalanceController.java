package faang.school.accountservice.controller;

import faang.school.accountservice.config.context.OwnershipChecker;
import faang.school.accountservice.dto.BalanceDto;
import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@RequiredArgsConstructor
@RequestMapping("/balance")
@RestController
public class BalanceController {

    private final BalanceService balanceService;
    private final BalanceMapper balanceMapper;
    private final OwnershipChecker ownershipChecker;

    @GetMapping("/{accountId}")
    public ResponseEntity<BalanceDto> getBalance(@PathVariable long accountId) {
        ownershipChecker.assertCanAccess(accountId);
        Balance balance = balanceService.getBalanceByAccountId(accountId);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }

    @PostMapping("/{accountId}")
    public ResponseEntity<BalanceDto> createBalance(@PathVariable long accountId) {
        ownershipChecker.assertCanAccess(accountId);
        Balance balance = balanceService.createBalance(accountId);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }

    @PostMapping("/{accountId}/authorizations")
    public ResponseEntity<BalanceDto> authorizeAmount(@PathVariable long accountId,
                                                      @RequestParam @Valid @Positive BigDecimal amount) {
        ownershipChecker.assertCanAccess(accountId);
        Balance balance = balanceService.authorizeAmount(accountId, amount);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }

    @PostMapping("/{accountId}/authorizations/commit")
    public ResponseEntity<BalanceDto> commitAuthorization(@PathVariable long accountId,
                                                          @RequestParam @Valid @Positive BigDecimal amount) {
        ownershipChecker.assertCanAccess(accountId);
        Balance balance = balanceService.commitAuthorization(accountId, amount);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }

    @PostMapping("/{accountId}/authorizations/cancel")
    public ResponseEntity<BalanceDto> cancelAuthorization(@PathVariable long accountId,
                                                          @RequestParam @Valid @Positive BigDecimal amount) {
        ownershipChecker.assertCanAccess(accountId);
        Balance balance = balanceService.cancelAuthorization(accountId, amount);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }

    @PostMapping("/{accountId}/credits")
    public ResponseEntity<BalanceDto> creditBalance(@PathVariable long accountId,
                                                    @RequestParam @Valid @Positive BigDecimal amount) {
        ownershipChecker.assertCanAccess(accountId);
        Balance balance = balanceService.creditBalance(accountId, amount);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }

    @PostMapping("/{accountId}/debits")
    public ResponseEntity<BalanceDto> debitBalance(@PathVariable long accountId,
                                                   @RequestParam @Valid @Positive BigDecimal amount) {
        ownershipChecker.assertCanAccess(accountId);
        Balance balance = balanceService.debitBalance(accountId, amount);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }
}
