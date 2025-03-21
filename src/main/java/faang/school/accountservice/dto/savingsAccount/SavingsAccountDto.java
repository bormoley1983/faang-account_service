package faang.school.accountservice.dto.savingsAccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsAccountDto {
    Long id;
    String number;
    BigDecimal balance;
    List<TariffHistorySnapshot> tariffHistory;
    LocalDate lastInterestDate;
}
