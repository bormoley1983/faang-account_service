package faang.school.accountservice.service;

import faang.school.accountservice.dto.savingsAccount.TariffHistorySnapshot;
import faang.school.accountservice.dto.savingsAccount.TariffSnapshot;
import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Proves the {@code @Retryable} proxy on
 * {@link InterestAccrualService#accrueInterestForAccount} is actually activated.
 * <p>
 * The test builds a minimal Spring context with {@code @EnableRetry} (the same
 * annotation now present on {@code AccountServiceApplication}) and the real
 * service bean, so the retry proxy is created exactly as in production.
 * <p>
 * Attempt counting uses a {@link RetryListener} registered as a bean: the
 * {@code @EnableRetry} infrastructure picks up every {@code RetryListener} bean
 * in the context and applies it to all retry templates (see
 * {@code RetryConfiguration#afterSingletonsInstantiated}), so no reflection into
 * Spring Retry internals is required.
 */
class InterestAccrualRetryTest {

    private static final long ACCOUNT_ID = 7L;
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 1);

    @Configuration
    @org.springframework.retry.annotation.EnableRetry
    static class RetryConfig {
        @Bean
        SavingsAccountRepository savingsAccountRepository() {
            return mock(SavingsAccountRepository.class);
        }

        @Bean
        TariffRepository tariffRepository() {
            return mock(TariffRepository.class);
        }

        @Bean
        InterestAccrualService interestAccrualService(SavingsAccountRepository savingsAccountRepository,
                                                      TariffRepository tariffRepository) {
            return new InterestAccrualService(savingsAccountRepository, tariffRepository);
        }

        @Bean
        RetryListener attemptCountingListener(AtomicInteger attempts) {
            return new RetryListener() {
                @Override
                public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                    attempts.set(1);
                    return true;
                }

                @Override
                public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                                                             Throwable throwable) {
                    attempts.incrementAndGet();
                }

                @Override
                public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
                                                           Throwable lastException) {
                    if (Boolean.TRUE.equals(context.getAttribute(RetryContext.EXHAUSTED))) {
                        attempts.decrementAndGet();
                    }
                }
            };
        }

        @Bean
        AtomicInteger attempts() {
            return new AtomicInteger();
        }
    }

    private AnnotationConfigApplicationContext context;
    private SavingsAccountRepository savingsAccountRepository;
    private TariffRepository tariffRepository;
    private InterestAccrualService service;
    private AtomicInteger attempts;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(RetryConfig.class);
        savingsAccountRepository = context.getBean(SavingsAccountRepository.class);
        tariffRepository = context.getBean(TariffRepository.class);
        service = context.getBean(InterestAccrualService.class);
        attempts = context.getBean(AtomicInteger.class);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void accrueInterestForAccount_isWrappedInRetryProxy() {
        // The bean must be a Spring AOP proxy so the @Retryable advice can run.
        assertThat(AopUtils.isAopProxy(service)).isTrue();
        assertThat(AopUtils.isCglibProxy(service)).isTrue();
    }

    @Test
    void transientFailureIsRetriedThenSurfacesToCaller() {
        // Arrange: the repository fails twice (transient), then succeeds.
        SavingsAccount account = savingsAccountWithBalance(new BigDecimal("1000.00"));
        when(savingsAccountRepository.findById(ACCOUNT_ID))
                .thenThrow(new RuntimeException("db connection reset"))
                .thenThrow(new RuntimeException("db connection reset"))
                .thenReturn(Optional.of(account));
        stubTariffForAccount(account);

        // Act: invoke through the proxy — retries happen inside the proxy.
        CompletableFuture<Void> result = service.accrueInterestForAccount(ACCOUNT_ID, TODAY);

        // Assert: the work completed after transient failures and was persisted once.
        result.join();
        assertThat(attempts.get())
                .as("the @Retryable proxy must retry the failed attempts")
                .isEqualTo(3);
        verify(savingsAccountRepository).save(account);
    }

    @Test
    void persistentFailureIsRetriedExhaustedTimesThenSurfacesToCaller() {
        // Arrange: the repository always fails.
        when(savingsAccountRepository.findById(ACCOUNT_ID))
                .thenThrow(new RuntimeException("db down"));

        // Act + Assert: the caller sees the failure after all retries are exhausted.
        assertThatThrownBy(() -> service.accrueInterestForAccount(ACCOUNT_ID, TODAY))
                .hasMessage("db down");

        // Default @Retryable policy: 3 attempts total (1 initial + 2 retries).
        assertThat(attempts.get())
                .as("the @Retryable proxy must exhaust its default 3 attempts")
                .isEqualTo(3);
        verify(savingsAccountRepository, never()).save(any());
    }

    private void stubTariffForAccount(SavingsAccount account) {
        Long tariffId = account.getTariffHistory().get(account.getTariffHistory().size() - 1)
                .getTariff().getId();
        Tariff tariff = Tariff.builder()
                .id(tariffId)
                .rateHistory(List.of(new BigDecimal("5.00")))
                .build();
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));
    }

    private SavingsAccount savingsAccountWithBalance(BigDecimal balance) {
        TariffSnapshot tariffSnapshot = TariffSnapshot.builder()
                .id(1L)
                .name("Basic")
                .rate(new BigDecimal("5.00"))
                .build();
        TariffHistorySnapshot snapshot = TariffHistorySnapshot.builder()
                .tariff(tariffSnapshot)
                .startDate(TODAY.minusDays(30))
                .endDate(null)
                .build();
        return SavingsAccount.builder()
                .id(ACCOUNT_ID)
                .balance(balance)
                .lastInterestDate(TODAY.minusDays(1))
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .tariffHistory(List.of(snapshot))
                .build();
    }
}
