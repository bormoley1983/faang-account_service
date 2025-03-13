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
import org.springframework.transaction.annotation.Transactional;

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
    private final Long ownerId = 1L;

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

        accountSeqRepository.save(new AccountSeq(AccountType.SAVINGS, 100));
        freeAccountRepository.save(new FreeAccountNumber(
                new FreeAccountId(AccountType.SAVINGS, 5536000000000100L)));
    }

    @Test
    void testFindById() {
        Optional<Account> account = accountRepository.findById(accountId);

        assertThat(account)
                .isPresent()
                .get()
                .satisfies(acc -> {
                    assertThat(acc.getStatus()).isEqualTo(AccountStatus.ACTIVE);
                    assertThat(acc.getType()).isEqualTo(AccountType.SAVINGS);
                    assertThat(acc.getCurrency()).isEqualTo(Currency.USD);
                });
    }

    @Test
    void testFindByOwnerId() {
        List<Account> accounts = accountRepository.findByOwnerId(ownerId);
        assertThat(accounts)
                .isNotEmpty()
                .allSatisfy(acc -> assertThat(acc.getOwnerId()).isEqualTo(ownerId));
    }

    @Test
    void testIncrementCounter_Success() {
        Optional<AccountSeq> result = accountSeqRepository.incrementCounter(AccountType.SAVINGS.name(), 5);

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(seq -> {
                    assertThat(seq.getType()).isEqualTo(AccountType.SAVINGS);
                    assertThat(seq.getCounter()).isGreaterThan(0);
                });
    }

    @Test
    void testRetrieveFirst_FreeAccountNumberExists() {
        Optional<FreeAccountNumber> freeAccountNumber = freeAccountRepository.retrieveFirst(AccountType.SAVINGS.name());

        assertThat(freeAccountNumber)
                .isPresent()
                .get()
                .satisfies(freeAcc -> {
                    assertThat(freeAcc.getId().getType()).isEqualTo(AccountType.SAVINGS);
                    assertThat(freeAcc.getId().getAccountNumber()).isEqualTo(5536000000000100L);
                });
    }

    @Test
    void testRetrieveFirst_NoFreeAccountNumber() {
        freeAccountRepository.deleteAll();
        Optional<FreeAccountNumber> freeAccountNumber = freeAccountRepository.retrieveFirst(AccountType.SAVINGS.name());
        assertThat(freeAccountNumber).isEmpty();
    }
}