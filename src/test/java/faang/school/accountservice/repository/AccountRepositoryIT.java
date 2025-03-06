package faang.school.accountservice.repository;

import faang.school.accountservice.config.TestContainersConfig;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.AccountSeq;
import faang.school.accountservice.model.FreeAccountId;
import faang.school.accountservice.model.FreeAccountNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class AccountRepositoryIT extends TestContainersConfig {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountSeqRepository accountSeqRepository;

    @Autowired
    private FreeAccountRepository freeAccountRepository;

    private Long accountId;
    private Long ownerId = 1L;

    @BeforeEach
    void setUp() {
        Account account = Account.builder()
                .number("123456789012")
                .ownerId(ownerId)
                .status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS)
                .currency(Currency.USD)
                .build();

        account = accountRepository.save(account);
        accountId = account.getId();

        AccountSeq accountSeq = new AccountSeq(AccountType.SAVINGS, 100);
        accountSeqRepository.save(accountSeq);

        FreeAccountNumber freeAccountNumber = new FreeAccountNumber(
                new FreeAccountId(AccountType.SAVINGS, 5536000000000100L));
        freeAccountRepository.save(freeAccountNumber);
    }

    @Test
    void testFindById() {
        Optional<Account> account = accountRepository.findById(accountId);
        assertThat(account).isPresent();
        assertThat(account.get().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.get().getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(account.get().getCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    void testFindByOwnerId() {
        List<Account> accounts = accountRepository.findByOwnerId(ownerId);
        assertThat(accounts).isNotEmpty();
        assertThat(accounts.get(0).getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void testIncrementCounter_Success() {
        List<Object[]> result = accountSeqRepository.incrementCounter(AccountType.SAVINGS.name(), 5);

        assertThat(result).isNotEmpty();
        Object[] row = result.get(0);
        assertThat(row[0]).isEqualTo("SAVINGS");
        assertThat((Long) row[1]).isEqualTo(105L);
        assertThat((Long) row[2]).isEqualTo(100L);
    }

    @Test
    void testRetrieveFirst_FreeAccountNumberExists() {
        FreeAccountNumber freeAccountNumber = freeAccountRepository.retrieveFirst(AccountType.SAVINGS.name());

        assertThat(freeAccountNumber).isNotNull();
        assertThat(freeAccountNumber.getId().getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(freeAccountNumber.getId().getAccountNumber()).isEqualTo(5536000000000100L);
    }

    @Test
    void testRetrieveFirst_NoFreeAccountNumber() {
        freeAccountRepository.deleteAll();

        FreeAccountNumber freeAccountNumber = freeAccountRepository.retrieveFirst(AccountType.SAVINGS.name());

        assertThat(freeAccountNumber).isNull();
    }
}