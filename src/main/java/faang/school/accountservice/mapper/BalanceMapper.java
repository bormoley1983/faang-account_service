package faang.school.accountservice.mapper;

import faang.school.accountservice.dto.BalanceDto;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.model.BalanceAudit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BalanceMapper {
    @Mapping(source = "account.id", target = "accountId")
    BalanceDto toDto(Balance balance);

    @Mapping(target = "account", expression = "java(Account.builder().id(balanceDto.getAccountId()).build())")
    Balance toEntity(BalanceDto balanceDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balanceId", source = "id")
    BalanceAudit toBalanceAudit(Balance balance);
}
