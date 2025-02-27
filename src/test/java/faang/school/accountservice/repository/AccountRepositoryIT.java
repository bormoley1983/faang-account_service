package faang.school.accountservice.repository;

import faang.school.accountservice.config.TestContainersConfig;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class AccountRepositoryIT extends TestContainersConfig {

    @Autowired
    private AccountRepository accountRepository;

    private Long accountId;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setNumber("123456789012");
        account.setOwnerId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setType(AccountType.SAVINGS);
        account.setCurrency(Currency.USD);

        account = accountRepository.save(account);
        accountId = account.getId();
    }

    @Test
    void testFindById() {
        Account account = accountRepository.findById(accountId).orElse(null);
        assertThat(account).isNotNull();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(account.getCurrency()).isEqualTo(Currency.USD);
    }
}