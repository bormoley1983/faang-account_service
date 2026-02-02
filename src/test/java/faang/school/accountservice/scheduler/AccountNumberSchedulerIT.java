package faang.school.accountservice.scheduler;

import faang.school.accountservice.config.BaseIntegrationTest;
import faang.school.accountservice.config.account.AccountProperties;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.model.AccountSeq;
import faang.school.accountservice.repository.AccountSeqRepository;
import faang.school.accountservice.repository.FreeAccountRepository;
import faang.school.accountservice.service.FreeAccountNumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AccountNumberSchedulerIT extends BaseIntegrationTest {

    @Autowired
    private AccountNumberScheduler accountNumberScheduler;

    @Autowired
    private FreeAccountNumberService freeAccountNumberService;

    @Autowired
    private FreeAccountRepository freeAccountRepository;

    @Autowired
    private AccountSeqRepository accountSeqRepository;

    @Autowired
    private AccountProperties accountProperties;

    @BeforeEach
    void setUp() {
        freeAccountRepository.deleteAll();
        accountSeqRepository.deleteAll();

        for (AccountType type : AccountType.values()) {
            long baseNumber = Long.parseLong(accountProperties.getBaseNumber(type.name()));
            accountSeqRepository.save(new AccountSeq(type, baseNumber));
        }

        accountNumberScheduler.generateAccounts();
    }

    @Test
    void testSchedulerCreatesRequiredAccounts() {
        for (AccountType type : AccountType.values()) {
            freeAccountNumberService.ensureSufficientAccountNumbers(type);
            long count = freeAccountRepository.countByType(type);

            assertThat(count)
                    .as("Number of available for " + type)
                    .isGreaterThan(0);
        }
    }

    @Test
    void testSchedulerCreatesMissingAccounts() {
        accountNumberScheduler.generateMissingAccounts();
        int expectedCount = 25;

        Arrays.stream(AccountType.values())
                .forEach(type -> {
                    int actualCount = freeAccountRepository.countByType(type);

                    assertEquals(expectedCount, actualCount);
                });
    }
}