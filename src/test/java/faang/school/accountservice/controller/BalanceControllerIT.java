package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.mapper.BalanceMapperImpl;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.repository.BalanceRepository;
import faang.school.accountservice.service.AccountService;
import faang.school.accountservice.service.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Import({BalanceService.class, BalanceMapperImpl.class})
@Testcontainers
@WebMvcTest(BalanceController.class)
public class BalanceControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withUsername("user")
            .withPassword("password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BalanceRepository balanceRepository;

    @MockBean
    private UserContext userContext;

    @MockBean
    private AccountService accountService;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private Balance testBalance;

    @BeforeEach
    void setup() {
        Account testAccount = Account.builder()
                .id(1L)
                .number("1234567890")
                .ownerId(100L)
                .type(AccountType.CHECKING)
                .currency(Currency.USD)
                .status(AccountStatus.ACTIVE)
                .build();

        testBalance = Balance.builder()
                .id(1L)
                .account(testAccount)
                .actualBalance(new BigDecimal("100.00"))
                .authorizedBalance(BigDecimal.ZERO)
                .version(1)
                .build();

        Mockito.when(balanceRepository.findByAccountId(1L)).thenReturn(testBalance);
        Mockito.when(accountService.getAccount(1L)).thenReturn(testAccount);
        Mockito.when(balanceRepository.save(Mockito.any(Balance.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testGetBalance() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/balance/{accountId}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("100.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("0"));
    }

    @Test
    public void testCreditBalance() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/credits", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("150.0"));
    }

    @Test
    public void testDebitBalance() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/debits", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("50.0"));
    }

    @Test
    public void testAuthorizeAmount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/authorizations", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("50.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("100.0"));
    }

    @Test
    public void testCommitAuthorization() throws Exception {
        testBalance.setAuthorizedBalance(new BigDecimal("50.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/authorizations/commit", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("0.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("50.0"));
    }

    @Test
    public void testCancelAuthorization() throws Exception {
        testBalance.setAuthorizedBalance(new BigDecimal("50.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/authorizations/cancel", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("0.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("100.0"));
    }

    @Test
    public void testOptimisticLockingOnConcurrentDebits() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        Mockito.when(balanceRepository.findByAccountId(1L))
                .thenAnswer(invocation -> {
                    Balance balance = Balance.builder()
                            .id(1L)
                            .account(testBalance.getAccount())
                            .actualBalance(testBalance.getActualBalance())
                            .authorizedBalance(testBalance.getAuthorizedBalance())
                            .version(testBalance.getVersion())
                            .build();
                    return balance;
                });

        Runnable task = () -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/debits", 1)
                                .param("amount", "60.00"))
                        .andExpect(MockMvcResultMatchers.status().isConflict());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        };

        executor.submit(task);
        executor.submit(task);
        latch.await();
        executor.shutdown();
    }
}
