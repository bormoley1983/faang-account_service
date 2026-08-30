package faang.school.accountservice.controller;

import faang.school.accountservice.dto.tariff.RegisterTariffDto;
import faang.school.accountservice.dto.tariff.TariffDto;
import faang.school.accountservice.mapper.TariffMapped;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.service.TariffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffControllerTest {

    private static final Long TARIFF_ID = 1L;
    private static final String NAME = "standard";
    private static final BigDecimal RATE = new BigDecimal("5.00");

    @Mock
    private TariffService tariffService;

    @Mock
    private TariffMapped tariffMapped;

    @InjectMocks
    private TariffController tariffController;

    private Tariff tariff;
    private TariffDto tariffDto;

    @BeforeEach
    void setUp() {
        tariff = Tariff.builder()
                .name(NAME)
                .rateHistory(List.of(RATE))
                .build();

        tariffDto = TariffDto.builder()
                .name(NAME)
                .rateHistory(List.of(RATE))
                .build();
    }

    @Test
    void addTariff_delegatesToServiceAndReturnsMappedDto() {
        RegisterTariffDto registerDto = new RegisterTariffDto(NAME, RATE);
        when(tariffService.addTariff(NAME, RATE)).thenReturn(tariff);
        when(tariffMapped.toDto(tariff)).thenReturn(tariffDto);

        ResponseEntity<TariffDto> response = tariffController.addTariff(registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tariffDto);
        verify(tariffService).addTariff(NAME, RATE);
    }

    @Test
    void changeTariff_delegatesToServiceAndReturnsMappedDto() {
        RegisterTariffDto registerDto = new RegisterTariffDto(NAME, RATE);
        when(tariffService.changeTariff(TARIFF_ID, NAME, RATE)).thenReturn(tariff);
        when(tariffMapped.toDto(tariff)).thenReturn(tariffDto);

        ResponseEntity<TariffDto> response = tariffController.changeTariff(TARIFF_ID, registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(tariffService).changeTariff(TARIFF_ID, NAME, RATE);
    }

    @Test
    void getTariff_delegatesToServiceAndReturnsMappedDto() {
        when(tariffService.getTariff(TARIFF_ID)).thenReturn(tariff);
        when(tariffMapped.toDto(tariff)).thenReturn(tariffDto);

        ResponseEntity<TariffDto> response = tariffController.getTariff(TARIFF_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(tariffService).getTariff(TARIFF_ID);
    }
}
