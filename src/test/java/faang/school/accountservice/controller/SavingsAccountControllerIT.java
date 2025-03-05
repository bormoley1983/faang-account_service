package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.TestContainersConfig;
import faang.school.accountservice.dto.savingsAccount.AmountDto;
import faang.school.accountservice.dto.savingsAccount.SavingsAccountRegisterDto;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.repository.SavingsAccountRepository;
import faang.school.accountservice.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class SavingsAccountControllerIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testAccountId;

    private Long testTariffId;

    @BeforeEach
    void clean() {
        savingsAccountRepository.deleteAll();
        accountRepository.deleteAll();
        tariffRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        String uniqueNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Account account = new Account();
        account.setNumber(uniqueNumber);
        account.setOwnerId(1L);
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

        mockMvc.perform(get("/savingsAccount/owner/1")
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
        deposit.setAmount(new BigDecimal("0"));

        mockMvc.perform(post("/savingsAccount/" + testAccountId + "/withdraw")
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());
    }
}
