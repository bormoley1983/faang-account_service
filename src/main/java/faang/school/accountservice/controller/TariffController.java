package faang.school.accountservice.controller;

import faang.school.accountservice.dto.tariff.RegisterTariffDto;
import faang.school.accountservice.dto.tariff.TariffDto;
import faang.school.accountservice.mapper.TariffMapped;
import faang.school.accountservice.model.Tariff;
import faang.school.accountservice.service.TariffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/tariff")
@RestController
public class TariffController {

    private final TariffService tariffService;

    private final TariffMapped tariffMapped;

    @PostMapping
    public ResponseEntity<TariffDto> addTariff(@Valid @RequestBody RegisterTariffDto tariffDto) {
        Tariff tariff = tariffService.addTariff(tariffDto.getName(), tariffDto.getRate());
        return ResponseEntity.ok(tariffMapped.toDto(tariff));
    }

    @PostMapping("/{tariffId}")
    public ResponseEntity<TariffDto> changeTariff(@PathVariable Long tariffId,@Valid @RequestBody RegisterTariffDto tariffDto) {
        Tariff tariff = tariffService.changeTariff(tariffId, tariffDto.getName(), tariffDto.getRate());
        return ResponseEntity.ok(tariffMapped.toDto(tariff));
    }

    @GetMapping("/{tariffId}")
    public ResponseEntity<TariffDto> getTariff(@PathVariable Long tariffId) {
        Tariff tariff = tariffService.getTariff(tariffId);
        return ResponseEntity.ok(tariffMapped.toDto(tariff));
    }
}


