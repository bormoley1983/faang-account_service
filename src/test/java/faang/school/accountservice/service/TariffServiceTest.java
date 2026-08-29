package faang.school.accountservice.service;

import faang.school.accountservice.exeption.TariffNotFound;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.TariffRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TariffServiceTest {

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private TariffService tariffService;

    @Test
    void addTariff() {
        String name = "standart";
        BigDecimal prozent = new BigDecimal("100");
        Tariff tariff = Tariff.builder()
                .name(name)
                .build();

        tariffService.addTariff(name, prozent);
        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);
        Mockito.verify(tariffRepository)
                .save(captor.capture());
        Tariff capturedValue = captor.getValue();

        Assertions.assertEquals(tariff.getName(), capturedValue.getName());

    }

    @Test
    void changeTariffAppendsRateWithoutDestroyingHistory(){
        Long tariffId = 1L;
        String name = "standard";
        BigDecimal newRate = new BigDecimal("10.00");
        List<BigDecimal> existingHistory = List.of(
                new BigDecimal("5.00"),
                new BigDecimal("7.50")
        );
        Tariff tariff = Tariff.builder()
                .name("old name")
                .rateHistory(existingHistory)
                .build();

        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));
        tariffService.changeTariff(tariffId, name, newRate);
        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);
        Mockito.verify(tariffRepository).save(captor.capture());
        Tariff capturedValue = captor.getValue();

        Assertions.assertEquals(name, capturedValue.getName());
        Assertions.assertEquals(
                List.of(new BigDecimal("5.00"), new BigDecimal("7.50"), newRate),
                capturedValue.getRateHistory()
        );
        Assertions.assertNotSame(existingHistory, capturedValue.getRateHistory());
    }

    @Test
    void changeTariffNotFound() {
        Long tariffId = 1L;
        String name = "standart";
        BigDecimal prozent = new BigDecimal("100");
        Mockito.when(tariffRepository.findById(tariffId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(TariffNotFound.class ,
                () -> tariffService.changeTariff(tariffId, name, prozent));
    }

    @Test
    void getTariff(){
        Long tariffId = 1L;
        Tariff tariff = new Tariff();
        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));
        Tariff result = tariffService.getTariff(tariffId);

        Assertions.assertEquals(tariff, result);
        Mockito.verify(tariffRepository, Mockito.times(1)).findById(tariffId);
    }

    @Test
    void getTariffNotFound(){
        Long tariffId = 1L;
        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.empty());

        Assertions.assertThrows(TariffNotFound.class,
                ()-> tariffService.getTariff(tariffId));
    }
}
