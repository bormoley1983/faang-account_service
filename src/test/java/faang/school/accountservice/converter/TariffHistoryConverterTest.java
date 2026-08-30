package faang.school.accountservice.converter;

import faang.school.accountservice.dto.savingsAccount.TariffHistorySnapshot;
import faang.school.accountservice.dto.savingsAccount.TariffSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TariffHistoryConverterTest {

    private final TariffHistoryConverter converter = new TariffHistoryConverter();

    @Test
    void convertToDatabaseColumn_whenNull_returnsEmptyJsonArray() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("[]");
    }

    @Test
    void convertToDatabaseColumn_whenEmpty_returnsEmptyJsonArray() {
        assertThat(converter.convertToDatabaseColumn(List.of())).isEqualTo("[]");
    }

    @Test
    void convertToDatabaseColumn_whenPresent_serializesToJson() {
        TariffSnapshot snapshot = TariffSnapshot.builder()
                .id(1L)
                .name("standard")
                .rate(new BigDecimal("5.00"))
                .build();
        TariffHistorySnapshot history = TariffHistorySnapshot.builder()
                .tariff(snapshot)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();

        String json = converter.convertToDatabaseColumn(List.of(history));

        assertThat(json).contains("standard").contains("5.0");
    }

    @Test
    void convertToEntityAttribute_whenNull_returnsEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
    }

    @Test
    void convertToEntityAttribute_whenBlank_returnsEmptyList() {
        assertThat(converter.convertToEntityAttribute("   ")).isEmpty();
    }

    @Test
    void convertToEntityAttribute_whenValidJson_roundTrips() {
        TariffSnapshot snapshot = TariffSnapshot.builder()
                .id(1L)
                .name("standard")
                .rate(new BigDecimal("5.00"))
                .build();
        TariffHistorySnapshot history = TariffHistorySnapshot.builder()
                .tariff(snapshot)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();

        String json = converter.convertToDatabaseColumn(List.of(history));
        List<TariffHistorySnapshot> result = converter.convertToEntityAttribute(json);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTariff().getName()).isEqualTo("standard");
        assertThat(result.get(0).getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void convertToEntityAttribute_whenInvalidJson_throwsIllegalArgument() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("{not-json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error converting JSON to list");
    }
}
