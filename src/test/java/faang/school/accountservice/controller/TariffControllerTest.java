package faang.school.accountservice.controller;

import faang.school.accountservice.config.context.OwnershipChecker;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
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

    @Mock
    private OwnershipChecker ownershipChecker;

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

    // ── addTariff ──────────────────────────────────────────────────────────────

    @Test
    void addTariff_adminAllowed_delegatesToServiceAndReturnsMappedDto() {
        RegisterTariffDto registerDto = new RegisterTariffDto(NAME, RATE);
        when(tariffService.addTariff(NAME, RATE)).thenReturn(tariff);
        when(tariffMapped.toDto(tariff)).thenReturn(tariffDto);

        ResponseEntity<TariffDto> response = tariffController.addTariff(registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tariffDto);
        verify(ownershipChecker).assertAdmin();
        verify(tariffService).addTariff(NAME, RATE);
    }

    @Test
    void addTariff_nonAdminRejected_throwsSecurityException() {
        RegisterTariffDto registerDto = new RegisterTariffDto(NAME, RATE);
        org.mockito.Mockito.doThrow(new SecurityException("not an admin"))
                .when(ownershipChecker).assertAdmin();

        assertThatThrownBy(() -> tariffController.addTariff(registerDto))
                .isInstanceOf(SecurityException.class)
                .hasMessage("not an admin");

        verify(tariffService, never()).addTariff(NAME, RATE);
    }

    // ── changeTariff ───────────────────────────────────────────────────────────

    @Test
    void changeTariff_adminAllowed_delegatesToServiceAndReturnsMappedDto() {
        RegisterTariffDto registerDto = new RegisterTariffDto(NAME, RATE);
        when(tariffService.changeTariff(TARIFF_ID, NAME, RATE)).thenReturn(tariff);
        when(tariffMapped.toDto(tariff)).thenReturn(tariffDto);

        ResponseEntity<TariffDto> response = tariffController.changeTariff(TARIFF_ID, registerDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ownershipChecker).assertAdmin();
        verify(tariffService).changeTariff(TARIFF_ID, NAME, RATE);
    }

    @Test
    void changeTariff_nonAdminRejected_throwsSecurityException() {
        RegisterTariffDto registerDto = new RegisterTariffDto(NAME, RATE);
        org.mockito.Mockito.doThrow(new SecurityException("not an admin"))
                .when(ownershipChecker).assertAdmin();

        assertThatThrownBy(() -> tariffController.changeTariff(TARIFF_ID, registerDto))
                .isInstanceOf(SecurityException.class)
                .hasMessage("not an admin");

        verify(tariffService, never()).changeTariff(TARIFF_ID, NAME, RATE);
    }

    // ── getTariff ──────────────────────────────────────────────────────────────

    @Test
    void getTariff_authenticatedUser_delegatesToServiceAndReturnsMappedDto() {
        when(tariffService.getTariff(TARIFF_ID)).thenReturn(tariff);
        when(tariffMapped.toDto(tariff)).thenReturn(tariffDto);

        ResponseEntity<TariffDto> response = tariffController.getTariff(TARIFF_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ownershipChecker).assertAuthenticated();
        verify(tariffService).getTariff(TARIFF_ID);
    }

    @Test
    void getTariff_unauthenticatedUser_throwsSecurityException() {
        org.mockito.Mockito.doThrow(new SecurityException("Authenticated user is required"))
                .when(ownershipChecker).assertAuthenticated();

        assertThatThrownBy(() -> tariffController.getTariff(TARIFF_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Authenticated user is required");

        verify(tariffService, never()).getTariff(TARIFF_ID);
    }
}
