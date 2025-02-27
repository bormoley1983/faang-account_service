package faang.school.accountservice.mapper;

import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountDto toDto(Account account);

    Account toEntity(AccountDto accountDto);
}