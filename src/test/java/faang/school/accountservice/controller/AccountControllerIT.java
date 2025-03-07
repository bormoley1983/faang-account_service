package faang.school.accountservice.controller;

import faang.school.accountservice.config.TestContainersConfig;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.FreeAccountRepository;
import faang.school.accountservice.service.FreeAccountNumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AccountControllerIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FreeAccountRepository freeAccountRepository;

    @Autowired
    private FreeAccountNumberService freeAccountNumberService;

    private Long accountId;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        freeAccountRepository.deleteAll();

        freeAccountNumberService.generateAccountNumbers(AccountType.CHECKING, 10);
        freeAccountNumberService.generateAccountNumbers(AccountType.SAVINGS, 10);
        freeAccountNumberService.generateAccountNumbers(AccountType.DEBIT, 10);
        freeAccountNumberService.generateAccountNumbers(AccountType.CURRENCY, 10);

        Account account = Account.builder()
                .number("123456789012")
                .ownerId(1L)
                .type(AccountType.CHECKING)
                .currency(faang.school.accountservice.enums.Currency.EUR)
                .status(AccountStatus.ACTIVE)
                .build();

        account = accountRepository.saveAndFlush(account);
        accountId = account.getId();

        ensureTestAccountNumbers();

        System.out.println("Account ID created: " + accountId);
        assertThat(accountId).isNotNull();
    }

    void ensureTestAccountNumbers() {
        int freeCount = freeAccountRepository.countByType(AccountType.CHECKING);
        if (freeCount < 10) {
            System.out.println("Generating additional account numbers...");
            freeAccountNumberService.generateAccountNumbers(AccountType.CHECKING, 10);
        }
    }

    @Test
    void testGetAccount() throws Exception {
        mockMvc.perform(get("/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId));
    }

    @Test
    void testCreateAccount() throws Exception {
        String accountJson = """
                {
                    "number": "987654321098",
                    "ownerId": 2,
                    "type": "CHECKING",
                    "currency": "EUR",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "2")
                        .content(accountJson))
                .andExpect(status().isOk());

        assertThat(accountRepository.count()).isGreaterThan(0);
    }

    @Test
    void testBlockAccount() throws Exception {
        mockMvc.perform(patch("/accounts/{id}/block", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "1"))
                .andExpect(status().isOk());

        Account updatedAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(updatedAccount.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }

    @Test
    void testCloseAccount() throws Exception {
        mockMvc.perform(patch("/accounts/{id}/close", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccountNotFound() throws Exception {
        mockMvc.perform(get("/accounts/{id}", 9999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testConcurrentAccountCreation() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    String accountJson = """
                                {
                                    "number": "987654321098",
                                    "ownerId": 2,
                                    "type": "CHECKING",
                                    "currency": "EUR",
                                    "status": "ACTIVE"
                                }
                            """;
                    mockMvc.perform(post("/accounts")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("x-user-id", "2")
                                    .content(accountJson))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertThat(accountRepository.count()).isGreaterThan(0);
    }
}