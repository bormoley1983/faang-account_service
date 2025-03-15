package faang.school.accountservice.scheduler;

import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.service.FreeAccountNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@RequiredArgsConstructor
@Component
public class AccountNumberScheduler {

    @Value("${account.number.batch-size}")
    private int batchSize;

    @Value("${account.number.max-free}")
    private int maxFreeNumbers;

    private final FreeAccountNumberService freeAccountNumberService;

    @Scheduled(cron = "0 0 0 * * *")
    public void generateAccounts() {
        Arrays.stream(AccountType.values())
                .forEach(type -> freeAccountNumberService.generateAccountNumbers(type, batchSize));
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void generateMissingAccounts() {
        Arrays.stream(AccountType.values())
                .forEach(type -> freeAccountNumberService.generateMissingAccountNumbers(type, maxFreeNumbers));
    }
}