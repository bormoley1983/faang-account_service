package faang.school.accountservice.service;

import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.model.BalanceAudit;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.BalanceAuditRepository;
import faang.school.accountservice.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class BalanceAuditServiceIT {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private BalanceAuditRepository balanceAuditRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Balance balance;

    private Account account;

    @BeforeEach
    public void setup() {
        account = new Account();
        account.setNumber("123456789012");
        account.setOwnerId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setType(AccountType.SAVINGS);
        account.setCurrency(Currency.USD);

        accountRepository.save(account);

        balance = balance.builder()
                .id(1L)
                .account(account)
                .actualBalance(BigDecimal.ZERO)
                .authorizedBalance(BigDecimal.ZERO)
                .build();
    }

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Test
    public void balanceAuditingIT() {
        int actualBalance = 100;
        int withdrawAmount = 200;
        List<Integer> expectedVersions = List.of(1, 2, 2);
        List<BigDecimal> expectedBalances = List.of(BigDecimal.ZERO.setScale(2),
                BigDecimal.valueOf(actualBalance).setScale(2),
                BigDecimal.valueOf(actualBalance).setScale(2));

        Balance bal = balanceService.createBalance(1L);
        balanceService.creditBalance(1L, BigDecimal.valueOf(actualBalance));

        assertThrows(IllegalStateException.class, () -> balanceService.authorizeAmount(1L, BigDecimal.valueOf(withdrawAmount)));

        List<BalanceAudit> audits = balanceAuditRepository.findAll();
        List<Integer> actualVersions = audits
                .stream()
                .map(BalanceAudit::getVersion)
                .toList();
        List<BigDecimal> actualBalances = audits
                .stream()
                .map((audit) -> audit.getActualBalance().setScale(2))
                .toList();

        assertEquals(3, audits.size());
        assertEquals(expectedVersions, actualVersions);
        assertEquals(expectedBalances, actualBalances);
    }
}
