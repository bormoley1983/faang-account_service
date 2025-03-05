package faang.school.accountservice.mapper;

import faang.school.accountservice.dto.savingsAccount.SavingsAccountDto;
import faang.school.accountservice.model.SavingsAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SavingsAccountMapper {

    @Mapping(target = "number", source = "account.number")
    SavingsAccountDto toDto(SavingsAccount savingsAccount);
}
