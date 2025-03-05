package faang.school.accountservice.dto.savingsAccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffSnapshot {
    private Long id;
    private String name;
    private BigDecimal rate;
}
