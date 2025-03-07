package faang.school.accountservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceDto {

    @NotNull
    @Positive
    private Long id;

    @NotNull
    @Positive
    private Long accountId;

    @Positive
    private BigDecimal authorizedBalance;

    @Positive
    private BigDecimal actualBalance;
}
