package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.context.UserContext;
import faang.school.accountservice.dto.BalanceDto;
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
    public void testCreateBalance() throws Exception {
        BalanceDto balanceDto = new BalanceDto(1L, BigDecimal.ZERO, new BigDecimal("200.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(balanceDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("200.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("0"));
    }

    @Test
    public void testUpdateBalance() throws Exception {
        BalanceDto updatedBalanceDto = new BalanceDto(1L, BigDecimal.ZERO, new BigDecimal("300.00"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBalanceDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("300.0"));
    }

    @Test
    public void testAuthorizePayment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/authorize", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("50.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("100.0"));
    }

    @Test
    public void testCapturePayment() throws Exception {
        testBalance.setAuthorizedBalance(new BigDecimal("50.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/capture", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("0.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("50.0"));
    }

    @Test
    public void testCancelAuthorization() throws Exception {
        testBalance.setAuthorizedBalance(new BigDecimal("50.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/balance/{accountId}/cancel-authorization", 1)
                        .param("amount", "50.00"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorizedBalance").value("0.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.actualBalance").value("100.0"));
    }
}
