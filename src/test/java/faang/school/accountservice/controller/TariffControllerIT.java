package faang.school.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.accountservice.config.TestContainersConfig;
import faang.school.accountservice.dto.tariff.RegisterTariffDto;
import faang.school.accountservice.model.Tariff;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class TariffControllerIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testTariffId;

    @BeforeEach
    void setUp() {
        tariffRepository.deleteAll();
        Tariff tariff = new Tariff();
        tariff.setName("name");
        tariff.setRateHistory(List.of(new BigDecimal("1"), new BigDecimal("2"),
                        new BigDecimal("3"), new BigDecimal("4")));

        tariffRepository.save(tariff);
        testTariffId = tariff.getId();
    }

    @Test
    void testGetTariff() throws Exception {
        mockMvc.perform(get("/tariff/" + testTariffId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"));
    }

    @Test
    void testAddTariff() throws Exception {
        RegisterTariffDto request = RegisterTariffDto.builder()
                .name("Premium Plan")
                .rateHistory(new BigDecimal("19.99"))
                .build();

        mockMvc.perform(post("/tariff")
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Premium Plan"))
                .andExpect(jsonPath("$.rateHistory[0]").value(19.99));
    }

    @Test
    void testChangeTariff() throws Exception {
        RegisterTariffDto request = RegisterTariffDto.builder()
                .name("Premium Plan")
                .rateHistory(new BigDecimal("19.99"))
                .build();

        mockMvc.perform(post("/tariff/" + testTariffId)
                        .header("x-user-id", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Premium Plan"))
                .andExpect(jsonPath("$.rateHistory[0]").value(19.99));
    }
}
