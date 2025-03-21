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
    void changeTariff(){
        Long tariffId = 1L;
        String name = "standart";
        BigDecimal prozent = new BigDecimal("100");
        Tariff tariff = new Tariff();
        List<BigDecimal> prozents = tariff.getRateHistory();
        prozents.add(prozent);
        tariff.setRateHistory(prozents);

        Mockito.when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));
        tariffService.changeTariff(tariffId, name, prozent);
        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);
        Mockito.verify(tariffRepository).save(captor.capture());
        Tariff capturedValue = captor.getValue();

        Assertions.assertEquals(tariff.getName(), capturedValue.getName());
        Assertions.assertEquals(tariff.getRateHistory(),capturedValue.getRateHistory());
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
