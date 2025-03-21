package faang.school.accountservice.dto.tariff;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffDto {
    @NotNull
    private String name;
    @NotNull
    private List<BigDecimal> rateHistory;
}
