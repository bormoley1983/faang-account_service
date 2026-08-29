package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import faang.school.accountservice.config.BaseIntegrationTest;
import faang.school.accountservice.dto.savingsAccount.AmountDto;
import faang.school.accountservice.dto.savingsAccount.SavingsAccountRegisterDto;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.BalanceRepository;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffRepository;
import faang.school.accountservice.repository.BalanceAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class SavingsAccountControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private BalanceAuditRepository balanceAuditRepository;

    private Long testAccountId;
    private Long testTariffId;

    @BeforeEach
    void setUp() {
        balanceAuditRepository.deleteAll();
        savingsAccountRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteAll();
        tariffRepository.deleteAll();

        String uniqueNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Account account = new Account();
        account.setNumber(uniqueNumber);
        account.setOwnerId(12345L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setType(AccountType.SAVINGS);
        account.setCurrency(Currency.USD);
        testAccountId = accountRepository.save(account).getId();

        Tariff tariff = Tariff.builder()
                .name("Standard Tariff")
                .rateHistory(List.of(new BigDecimal("5.0")))
                .build();
        testTariffId = tariffRepository.save(tariff).getId();
    }

    @Test
    void testGetSavingsAccount() throws Exception {
        SavingsAccountRegisterDto request = SavingsAccountRegisterDto.builder()
                .tariffId(testTariffId)
                .build();

        mockMvc.perform(post("/savingsAccount/" + testAccountId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/savingsAccount/" + testAccountId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccountId))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.tariffHistory[0].tariff.name").value("Standard Tariff"))
                .andExpect(jsonPath("$.tariffHistory[0].tariff.rate").value(5.0));
    }

    @Test
    void testOpenSavingsAccount() throws Exception {
        SavingsAccountRegisterDto request = SavingsAccountRegisterDto.builder()
                .tariffId(testTariffId)
                .build();

        mockMvc.perform(post("/savingsAccount/" + testAccountId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tariffHistory[0].tariff.id").value(testTariffId))
                .andExpect(jsonPath("$.tariffHistory[0].tariff.name").value("Standard Tariff"))
                .andExpect(jsonPath("$.tariffHistory[0].tariff.rate").value(5.0));
    }

    @Test
    void testGetSavingsAccountByOwnerId() throws Exception {
        SavingsAccountRegisterDto request = SavingsAccountRegisterDto.builder()
                .tariffId(testTariffId)
                .build();

        mockMvc.perform(post("/savingsAccount/" + testAccountId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/savingsAccount/owner/12345")
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccountId))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.tariffHistory[0].tariff.name").value("Standard Tariff"))
                .andExpect(jsonPath("$.tariffHistory[0].tariff.rate").value(5.0));
    }

    @Test
    void testDeposit() throws Exception {
        SavingsAccountRegisterDto request = SavingsAccountRegisterDto.builder()
                .tariffId(testTariffId)
                .build();

        mockMvc.perform(post("/savingsAccount/" + testAccountId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        AmountDto deposit = new AmountDto();
        deposit.setAmount(new BigDecimal("10"));

        mockMvc.perform(post("/savingsAccount/" + testAccountId + "/deposit")
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());
    }

    @Test
    void testWithdraw() throws Exception {
        SavingsAccountRegisterDto request = SavingsAccountRegisterDto.builder()
                .tariffId(testTariffId)
                .build();

        mockMvc.perform(post("/savingsAccount/" + testAccountId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        AmountDto deposit = new AmountDto();
        deposit.setAmount(new BigDecimal("10"));

        mockMvc.perform(post("/savingsAccount/" + testAccountId + "/deposit")
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());

        AmountDto withdraw = new AmountDto();
        withdraw.setAmount(new BigDecimal("5"));

        mockMvc.perform(post("/savingsAccount/" + testAccountId + "/withdraw")
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdraw)))
                .andExpect(status().isOk());
    }
}