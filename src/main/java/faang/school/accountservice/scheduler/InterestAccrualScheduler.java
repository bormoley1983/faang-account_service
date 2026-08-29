package faang.school.accountservice.scheduler;

import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.service.InterestAccrualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
public class InterestAccrualScheduler {

    private final InterestAccrualService interestAccrualService;
    private final SavingsAccountRepository savingsAccountRepository;

    @Scheduled(cron = "${scheduler.interest.cron}")
    public void scheduledAccrual() {
        LocalDate today = LocalDate.now();
        log.info("Interest accrual started at {}", today);

        List<Long> accountIds = savingsAccountRepository.findAllAccountIdsRequiringInterest(today);

        List<CompletableFuture<Void>> futures = accountIds.stream()
                .map(accountId -> interestAccrualService.accrueInterestForAccount(accountId, today)
                        .whenComplete((ignored, exception) -> {
                            if (exception != null) {
                                log.error("Error accruing interest for account {}", accountId, exception);
                            }
                        })
                ).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Interest accrual completed");
    }
}
