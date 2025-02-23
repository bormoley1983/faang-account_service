package faang.school.accountservice.dto;

import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AccountDto(
        UUID id,
        String number,
        UUID ownerId,
        AccountType type,
        Currency currency,
        AccountStatus status
) {
}