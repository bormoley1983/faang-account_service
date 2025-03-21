package faang.school.accountservice.dto.tariff;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterTariffDto {
    @NotNull
    private String name;
    @NotNull
    private BigDecimal rateHistory;
}
