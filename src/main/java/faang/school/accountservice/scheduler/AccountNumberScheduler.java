package faang.school.accountservice.scheduler;

import faang.school.accountservice.config.context.ClusterLease;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.service.FreeAccountNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountNumberScheduler {

    private static final String LEASE_GENERATE = "account-number-generate";
    private static final String LEASE_MISSING = "account-number-missing";

    @Value("${account.number.batch-size}")
    private int batchSize;

    @Value("${account.number.max-free}")
    private int maxFreeNumbers;

    private final FreeAccountNumberService freeAccountNumberService;
    private final ClusterLease clusterLease;

    @Scheduled(cron = "0 0 0 * * *")
    public void generateAccounts() {
        if (!clusterLease.tryAcquire(LEASE_GENERATE)) {
            log.info("Skipping account number generation: lease held by another replica");
            return;
        }
        try {
            Arrays.stream(AccountType.values())
                    .forEach(type -> freeAccountNumberService.generateAccountNumbers(type, batchSize));
        } finally {
            clusterLease.release(LEASE_GENERATE);
        }
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void generateMissingAccounts() {
        if (!clusterLease.tryAcquire(LEASE_MISSING)) {
            log.info("Skipping missing account number generation: lease held by another replica");
            return;
        }
        try {
            Arrays.stream(AccountType.values())
                    .forEach(type -> freeAccountNumberService.generateMissingAccountNumbers(type, maxFreeNumbers));
        } finally {
            clusterLease.release(LEASE_MISSING);
        }
    }
}