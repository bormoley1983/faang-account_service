package faang.school.accountservice.service;

import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.FreeAccountId;
import faang.school.accountservice.model.FreeAccountNumber;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.AccountSeqRepository;
import faang.school.accountservice.repository.FreeAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.LongStream;

@RequiredArgsConstructor
@Slf4j
@Service
public class FreeAccountNumberService {

    @Value("${account.number.min-free}")
    private int minFreeNumbers;

    @Value("${account.number.batch-size}")
    private int generationBatchSize;

    private final AccountSeqRepository accountSeqRepository;
    private final FreeAccountRepository freeAccountRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void ensureSufficientAccountNumbers(AccountType type) {
        int availableNumbers = freeAccountRepository.countByType(type);
        log.info(" [{}] Checking available free account numbers for type {}: {}",
                Thread.currentThread().getId(), type, availableNumbers);

        if (availableNumbers < minFreeNumbers) {
            log.info(" [{}] Generating more account numbers for type {}",
                    Thread.currentThread().getId(), type);
            generateAccountNumbers(type, generationBatchSize);
        }
    }

    @Transactional
    public void generateAccountNumbers(AccountType type, int batchSize) {
        log.info(" [{}] Calling incrementCounter for type: {}, batchSize: {}",
                Thread.currentThread().getId(), type, batchSize);

        Optional<Object[]> result = accountSeqRepository.incrementCounter(type.name(), batchSize)
                .stream().findFirst();

        if (result.isEmpty()) {
            log.warn(" [{}] Failed to increment account sequence for type: {}",
                    Thread.currentThread().getId(), type);
            return;
        }

        Object[] row = result.get();
        log.info(" [{}] Received counter update for {}: {}",
                Thread.currentThread().getId(), type, row);

        long counter = ((Number) row[1]).longValue();
        long initialCounter = ((Number) row[2]).longValue();

        if (counter <= initialCounter) {
            log.warn(" [{}] Invalid counter values received for type {}: counter={}, initialCounter={}",
                    Thread.currentThread().getId(), type, counter, initialCounter);
            return;
        }

        LongStream.range(initialCounter, counter)
                .mapToObj(i -> new FreeAccountId(type, type.getBaseNumber() + i))
                .forEach(id -> {
                    log.info("🔍 [{}] Checking if account number exists: {}",
                            Thread.currentThread().getId(), id.getAccountNumber());

                    if (freeAccountRepository.existsById(id)) {
                        log.warn(" [{}] Skipping duplicate account number: {}",
                                Thread.currentThread().getId(), id.getAccountNumber());
                        return;
                    }

                    FreeAccountNumber freeAccountNumber = new FreeAccountNumber(id);
                    try {
                        freeAccountRepository.save(freeAccountNumber);
                        log.info(" [{}] Successfully saved account number: {}",
                                Thread.currentThread().getId(), id.getAccountNumber());
                    } catch (Exception e) {
                        log.error(" [{}] Failed to save account number: {} | Error: {}",
                                Thread.currentThread().getId(),
                                id.getAccountNumber(), e.getMessage());
                    }
                });

        int totalFreeNumbers = freeAccountRepository.countByType(type);
        log.info(" [{}] Successfully generated new account numbers for type {}. Total count: {}",
                Thread.currentThread().getId(), type, totalFreeNumbers);
    }

    @Transactional
    public void assignAccountNumber(Account account) {
        log.info("🔍 [{}] Assigning account number for new account, type: {}",
                Thread.currentThread().getId(), account.getType());

        ensureSufficientAccountNumbers(account.getType());

        FreeAccountNumber freeAccountNumber = freeAccountRepository.retrieveFirst(account.getType().name());

        if (freeAccountNumber == null) {
            log.error(" [{}] Account number generation failed. No free numbers available.",
                    Thread.currentThread().getId());
            throw new IllegalStateException("No free account numbers available");
        }

        log.info(" [{}] Assigned account number: {} to new account",
                Thread.currentThread().getId(), freeAccountNumber.getId().getAccountNumber());

        account.setNumber(String.valueOf(freeAccountNumber.getId().getAccountNumber()));
        accountRepository.save(account);
    }
}