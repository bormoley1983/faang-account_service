package faang.school.accountservice.mapper;

import faang.school.accountservice.dto.tariff.TariffDto;
import faang.school.accountservice.model.Tariff;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TariffMapped {

    TariffDto toDto(Tariff tariff);
}