package faang.school.accountservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDto {
    @NotNull(message = "Номер платежного счета не может быть null")
    private String number;
    @NotNull(message = "Тип платежного счета не может быть null")
    private String type;
    @NotNull(message = "Валюта не может быть null")
    private String currency;
    private Long userId;
    private Long projectId;
}
