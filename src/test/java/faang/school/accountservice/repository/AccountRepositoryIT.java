package faang.school.accountservice.repository;

import faang.school.accountservice.config.BaseIntegrationTest;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.AccountSeq;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.model.FreeAccountId;
import faang.school.accountservice.model.FreeAccountNumber;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class AccountRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountSeqRepository accountSeqRepository;

    @Autowired
    private FreeAccountRepository freeAccountRepository;

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    private Long accountId;
    private final Long ownerId = 1L;

    @BeforeEach
    void setUp() {
        savingsAccountRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();
        accountSeqRepository.deleteAll();
        freeAccountRepository.deleteAll();
        
        String uniqueAccountNumber = String.valueOf(System.nanoTime());

        Account account = Account.builder()
                .number(uniqueAccountNumber)
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

    @Test
    void databaseRejectsMoreThanOneBalanceForTheSameAccount() {
        Account account = accountRepository.findById(accountId).orElseThrow();
        balanceRepository.saveAndFlush(Balance.builder().account(account).build());

        assertThatThrownBy(() -> balanceRepository.saveAndFlush(
                Balance.builder().account(account).build()
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseAcceptsDebitAccountType() {
        Account debitAccount = Account.builder()
                .number(String.valueOf(System.nanoTime()))
                .ownerId(ownerId)
                .type(AccountType.DEBIT)
                .currency(Currency.USD)
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.saveAndFlush(debitAccount);

        assertThat(saved.getType()).isEqualTo(AccountType.DEBIT);
    }

    @Test
    void insertGeneratedBatchCreatesTheWholeRangeInOneStatement() {
        int inserted = freeAccountRepository.insertGeneratedBatch(
                AccountType.DEBIT.name(),
                4200000000000000L,
                100L,
                103L
        );

        assertThat(inserted).isEqualTo(3);
        assertThat(freeAccountRepository.findAll())
                .extracting(number -> number.getId().getAccountNumber())
                .contains(
                        4200000000000100L,
                        4200000000000101L,
                        4200000000000102L
                );
    }

    @Test
    void jpaOwnsTheBalanceVersionLifecycle() {
        Account account = accountRepository.findById(accountId).orElseThrow();
        Balance balance = balanceRepository.saveAndFlush(Balance.builder().account(account).build());

        assertThat(balance.getVersion()).isZero();

        balance.setActualBalance(BigDecimal.ONE);
        balance = balanceRepository.saveAndFlush(balance);

        assertThat(balance.getVersion()).isEqualTo(1);
    }

    @Test
    void deletingBalanceDoesNotCascadeToAccount() {
        Account account = accountRepository.findById(accountId).orElseThrow();
        Balance balance = balanceRepository.saveAndFlush(Balance.builder().account(account).build());
        Long balanceId = balance.getId();

        balanceRepository.delete(balance);
        balanceRepository.flush();

        assertThat(balanceRepository.existsById(balanceId)).isFalse();
        assertThat(accountRepository.existsById(accountId)).isTrue();
    }
}
