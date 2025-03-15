package faang.school.accountservice.service;

import faang.school.accountservice.config.account.AccountProperties;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.AccountSeq;
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
    private final AccountProperties accountProperties;

    @Transactional
    public void ensureSufficientAccountNumbers(AccountType type) {
        int availableNumbers = freeAccountRepository.countByType(type);
        log.info("Available free account numbers for {}: {}", type, availableNumbers);

        if (availableNumbers < minFreeNumbers) {
            log.info("Generating more account numbers for {}", type);
            generateAccountNumbers(type, generationBatchSize);
        }
    }

    @Transactional
    public void generateAccountNumbers(AccountType type, int batchSize) {
        fetchCounterUpdate(type, batchSize).ifPresent(seq -> {
            long baseNumber = parseBaseNumber(type);
            long counter = seq.getCounter();
            long initialCounter = counter - batchSize;

            if (counter > initialCounter) {
                processAccountNumbers(type, baseNumber, initialCounter, counter);
            } else {
                log.warn("Invalid counter values for {}: counter={}, initialCounter={}", type, counter, initialCounter);
            }
        });
    }

    @Transactional
    public void assignAccountNumber(Account account) {
        log.info("Assigning account number for {}", account.getType());

        ensureSufficientAccountNumbers(account.getType());

        freeAccountRepository.retrieveFirst(account.getType().name())
                .ifPresentOrElse(
                        freeAccountNumber -> {
                            account.setNumber(String.valueOf(freeAccountNumber.getId().getAccountNumber()));
                            accountRepository.save(account);
                            log.info("Assigned account number: {} to account", freeAccountNumber.getId().getAccountNumber());
                        },
                        () -> {
                            log.error("No available account numbers for {}", account.getType());
                            throw new IllegalStateException("No free account numbers available");
                        }
                );
    }

    private Optional<AccountSeq> fetchCounterUpdate(AccountType type, int batchSize) {
        log.info("Incrementing account sequence for {} with batch size {}", type, batchSize);

        return accountSeqRepository.incrementCounter(type.name(), batchSize)
                .or(() -> {
                    log.warn("Failed to increment sequence for {}", type);
                    return Optional.empty();
                });
    }

    private long parseBaseNumber(AccountType type) {
        try {
            return Long.parseLong(accountProperties.getBaseNumber(type.name()));
        } catch (NumberFormatException e) {
            log.error("Invalid base number format for {}: {}", type, e.getMessage());
            throw new IllegalStateException("Invalid base number format");
        }
    }

    private void processAccountNumbers(AccountType type, long baseNumber, long initialCounter, long counter) {
        LongStream.range(initialCounter, counter)
                .mapToObj(i -> new FreeAccountId(type, baseNumber + i))
                .forEach(this::saveAccountNumber);

        log.info("Generated new account numbers for {}. Total count: {}", type, freeAccountRepository.countByType(type));
    }

    private void saveAccountNumber(FreeAccountId id) {
        if (freeAccountRepository.existsById(id)) {
            log.warn("Skipping duplicate account number: {}", id.getAccountNumber());
            return;
        }

        try {
            freeAccountRepository.save(new FreeAccountNumber(id));
            log.info("Saved account number: {}", id.getAccountNumber());
        } catch (Exception e) {
            log.error("Failed to save account number: {} | Error: {}", id.getAccountNumber(), e.getMessage());
        }
    }
}