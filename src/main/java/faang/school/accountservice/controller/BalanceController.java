package faang.school.accountservice.controller;

import faang.school.accountservice.dto.BalanceDto;
import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
