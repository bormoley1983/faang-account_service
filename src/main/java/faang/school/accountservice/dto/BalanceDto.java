package faang.school.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceDto {
    private BigDecimal authorizedBalance;
    private BigDecimal actualBalance;
}
