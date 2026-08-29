package faang.school.accountservice.service.id;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UuidCreatorV7GeneratorTest {

    private final UuidV7Generator generator = new UuidCreatorV7Generator();

    @Test
    void generatesUniqueRfc9562Version7Uuids() {
        UUID first = generator.generate();
        UUID second = generator.generate();

        assertThat(first.version()).isEqualTo(7);
        assertThat(first.variant()).isEqualTo(2);
        assertThat(second.version()).isEqualTo(7);
        assertThat(second).isNotEqualTo(first);
    }
}
