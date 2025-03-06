package faang.school.accountservice.scheduler;

import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.service.FreeAccountNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AccountNumberScheduler {

    @Value("${account.number.batch-size}")
    private int batchSize;

    private final FreeAccountNumberService freeAccountNumberService;

    @Scheduled(cron = "0 0 0 * * *")
    public void generateAccounts() {
        for (AccountType type : AccountType.values()) {
            freeAccountNumberService.generateAccountNumbers(type, batchSize);
        }
    }
}