package faang.school.accountservice.scheduler;

import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.service.InterestAccrualService;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Component
public class InterestAccrualScheduler {

    private final InterestAccrualService interestAccrualService;
    private final SavingsAccountRepository savingsAccountRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Scheduled(cron = "${scheduler.interest.cron}")
    @Transactional
    public void scheduledAccrual() {
        LocalDate today = LocalDate.now();
        log.info("Interest accrual started at {}", today);

        List<SavingsAccount> accounts = savingsAccountRepository.findAllAccountsRequiringInterest(today);

        List<CompletableFuture<Void>> futures = accounts.stream()
                .map(account -> CompletableFuture.runAsync(
                        () -> interestAccrualService.accrueInterestForAccount(account, today),
                        executorService)
                        .exceptionally(ex -> {
                            log.error("Error accruing interest for account {}: {}", account.getId(), ex.getMessage(), ex);
                            return null;
                        })
                ).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Interest accrual completed");
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }
}
