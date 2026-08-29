package faang.school.accountservice.service;

import faang.school.accountservice.config.BaseIntegrationTest;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.BalanceAuditOutcome;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.BalanceAudit;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.BalanceAuditRepository;
import faang.school.accountservice.repository.BalanceRepository;
import faang.school.accountservice.repository.SavingsAccountRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class BalanceAuditServiceIT extends BaseIntegrationTest {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private BalanceAuditRepository balanceAuditRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;


    private Account account;

    @BeforeEach
    public void setup() {
        balanceAuditRepository.deleteAll();
        savingsAccountRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();

        account = new Account();
        account.setNumber(String.valueOf(System.nanoTime()));
        account.setOwnerId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setType(AccountType.SAVINGS);
        account.setCurrency(Currency.USD);

        accountRepository.save(account);

        balanceService.createBalance(account.getId());
    }

    @AfterEach
    public void tearDown() {
        balanceAuditRepository.deleteAll();
        savingsAccountRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void balanceAuditingIT() {
        int actualBalance = 100;
        int withdrawAmount = 200;
        List<Integer> expectedVersions = List.of(0, 1, 1);
        List<BigDecimal> expectedBalances = List.of(BigDecimal.ZERO.setScale(2),
                BigDecimal.valueOf(actualBalance).setScale(2),
                BigDecimal.valueOf(actualBalance).setScale(2));

        balanceService.creditBalance(account.getId(), BigDecimal.valueOf(actualBalance));

        assertThrows(IllegalStateException.class, () -> balanceService.authorizeAmount(account.getId(), BigDecimal.valueOf(withdrawAmount)));

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
        assertThat(audits)
                .extracting(BalanceAudit::getTransactionId)
                .doesNotContainNull()
                .doesNotHaveDuplicates()
                .allMatch(transactionId -> transactionId.version() == 7);
        assertThat(audits)
                .extracting(BalanceAudit::getOutcome)
                .containsExactly(
                        BalanceAuditOutcome.SUCCESS,
                        BalanceAuditOutcome.SUCCESS,
                        BalanceAuditOutcome.FAILED
                );
        assertThat(audits.get(2).getOperation()).isEqualTo("authorizeAmount");
        assertThat(audits.get(2).getFailureReason()).isEqualTo("Insufficient funds");
    }

    @Test
    public void shouldCreateAuditOnSuccessfulAuthorization() {
        BigDecimal creditAmount = BigDecimal.valueOf(500);
        BigDecimal authorizeAmount = BigDecimal.valueOf(300);

        balanceService.creditBalance(account.getId(), BigDecimal.valueOf(100));
        balanceService.creditBalance(account.getId(), creditAmount);
        balanceService.authorizeAmount(account.getId(), authorizeAmount);

        List<BalanceAudit> audits = balanceAuditRepository.findAll();

        assertThat(audits).hasSize(4);
        assertThat(audits.get(0).getVersion()).isZero();
        assertThat(audits.get(0).getActualBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        
        assertThat(audits.get(1).getVersion()).isEqualTo(1);
        assertThat(audits.get(1).getActualBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
        
        assertThat(audits.get(2).getVersion()).isEqualTo(2);
        assertThat(audits.get(2).getActualBalance()).isEqualByComparingTo(BigDecimal.valueOf(600));
        
        assertThat(audits.get(3).getVersion()).isEqualTo(3);
        assertThat(audits.get(3).getActualBalance()).isEqualByComparingTo(BigDecimal.valueOf(600));
    }

    @Test
    public void shouldTrackMultipleBalanceOperations() {
        balanceService.creditBalance(account.getId(), BigDecimal.valueOf(100));
        balanceService.creditBalance(account.getId(), BigDecimal.valueOf(50));
        balanceService.authorizeAmount(account.getId(), BigDecimal.valueOf(30));

        List<BalanceAudit> audits = balanceAuditRepository.findAll();

        assertThat(audits).hasSize(4);
        assertThat(audits.get(0).getActualBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(audits.get(1).getActualBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(audits.get(2).getActualBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(audits.get(3).getActualBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }
}
