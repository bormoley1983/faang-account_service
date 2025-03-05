package faang.school.accountservice.dto.savingsAccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffHistorySnapshot {
    private TariffSnapshot tariff;
    private LocalDate startDate;
    private LocalDate endDate;
}
