package faang.school.accountservice.service;

import faang.school.accountservice.exeption.TariffNotFound;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TariffService {
    private final TariffRepository tariffRepository;

    @Transactional
    public Tariff addTariff(String name, BigDecimal prozent) {
        Tariff tariff = Tariff.builder()
                .name(name)
                .rateHistory(List.of(prozent))
                .build();

        return tariffRepository.save(tariff);
    }

    @Transactional
    public Tariff changeTariff(Long tariffId, String name, BigDecimal prozent) {
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new TariffNotFound("Tariff not found"));

        tariff.setName(name);
        List<BigDecimal> rateHistory = tariff.getRateHistory() == null
                ? new ArrayList<>()
                : new ArrayList<>(tariff.getRateHistory());
        rateHistory.add(prozent);
        tariff.setRateHistory(rateHistory);

        return tariffRepository.save(tariff);
    }

    @Transactional(readOnly = true)
    public Tariff getTariff(Long tariffId) {
        return tariffRepository.findById(tariffId)
                .orElseThrow(() -> new TariffNotFound("Tariff not found"));
    }
}
