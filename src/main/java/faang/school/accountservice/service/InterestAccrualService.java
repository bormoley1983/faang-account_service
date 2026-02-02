package faang.school.accountservice.service;

import faang.school.accountservice.dto.savingsAccount.TariffHistorySnapshot;
import faang.school.accountservice.model.SavingsAccount;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@Service
public class InterestAccrualService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    private final SavingsAccountRepository savingsAccountRepository;
    private final TariffRepository tariffRepository;

    @Async("interestAccrualExecutor")
    @Retryable()
    public void accrueInterestForAccount(SavingsAccount account, LocalDate today) {

        LocalDate lastDate = account.getLastInterestDate();
        LocalDate startDate = (lastDate != null) ? lastDate.plusDays(1) : account.getCreatedAt().toLocalDate();
        long days = ChronoUnit.DAYS.between(startDate, today);

        if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (days > 0) {
            BigDecimal annualRate = getAnnualRate(getTariffId(account));
            BigDecimal dailyRate = annualRate
                    .divide(HUNDRED, MathContext.DECIMAL128)
                    .divide(DAYS_IN_YEAR, MathContext.DECIMAL128);

            BigDecimal interest = account.getBalance()
                    .multiply(dailyRate)
                    .multiply(new BigDecimal(days));

            account.setBalance(account.getBalance().add(interest));
            account.setLastInterestDate(today);

            savingsAccountRepository.save(account);
        }
    }

    private BigDecimal getAnnualRate(Long tariffId) {
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new EntityNotFoundException("Tariff not found"));

        List<BigDecimal> rateHistory = tariff.getRateHistory();

        if (rateHistory == null || rateHistory.isEmpty()) {
            throw new IllegalStateException("Tariff has no rate history");
        }

        return rateHistory.get(rateHistory.size() - 1);
    }

    private Long getTariffId(SavingsAccount account){
        List<TariffHistorySnapshot> history = account.getTariffHistory();
        if (history == null || history.isEmpty()) {
            throw new IllegalStateException("Tariff history is empty");
        }
        return history.get(history.size() - 1).getTariff().getId();
    }
}
