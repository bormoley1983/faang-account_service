package faang.school.accountservice.controller;

import faang.school.accountservice.dto.BalanceDto;
import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{accountId}")
    public ResponseEntity<BalanceDto> getBalance(@PathVariable long accountId) {
        Balance balance = balanceService.getBalanceByAccountId(accountId);
        BalanceDto balanceDto = balanceMapper.toDto(balance);
        return ResponseEntity.ok(balanceDto);
    }

    @PostMapping
    public ResponseEntity<BalanceDto> createBalance(@RequestBody BalanceDto balanceDto) {
        Balance newBalance = balanceMapper.toEntity(balanceDto);
        Balance actualBalance = balanceService.createBalance(newBalance);
        BalanceDto actualBalanceDto = balanceMapper.toDto(actualBalance);
        return ResponseEntity.ok(actualBalanceDto);
    }

    @PatchMapping
    public ResponseEntity<BalanceDto> updateBalance(@RequestBody BalanceDto balanceDto) {
        Balance newBalance = balanceMapper.toEntity(balanceDto);
        Balance actualBalance = balanceService.updateBalance(newBalance);
        BalanceDto actualBalanceDto = balanceMapper.toDto(actualBalance);
        return ResponseEntity.ok(actualBalanceDto);
    }

    @PostMapping("/{accountId}/authorize")
    public ResponseEntity<BalanceDto> authorizePayment(
            @PathVariable long accountId,
            @RequestParam BigDecimal amount
    ) {
        Balance balance = balanceService.authorizePayment(accountId, amount);
        return ResponseEntity.ok(balanceMapper.toDto(balance));
    }

    @PostMapping("/{accountId}/capture")
    public ResponseEntity<BalanceDto> capturePayment(
            @PathVariable long accountId,
            @RequestParam BigDecimal amount
    ) {
        Balance balance = balanceService.capturePayment(accountId, amount);
        return ResponseEntity.ok(balanceMapper.toDto(balance));
    }

    @PostMapping("/{accountId}/cancel-authorization")
    public ResponseEntity<BalanceDto> cancelAuthorization(
            @PathVariable long accountId,
            @RequestParam BigDecimal amount
    ) {
        Balance balance = balanceService.cancelAuthorization(accountId, amount);
        return ResponseEntity.ok(balanceMapper.toDto(balance));
    }
}
