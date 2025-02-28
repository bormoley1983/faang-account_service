package faang.school.accountservice.mapper;

import faang.school.accountservice.dto.BalanceDto;
import faang.school.accountservice.model.Balance;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BalanceMapper {
    BalanceDto toDto(Balance balance);
}
