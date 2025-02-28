package faang.school.accountservice.dto;

import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import lombok.Builder;

@Builder
public record AccountDto(
        Long id,
        String number,
        Long ownerId,
        AccountType type,
        Currency currency,
        AccountStatus status
) {
}