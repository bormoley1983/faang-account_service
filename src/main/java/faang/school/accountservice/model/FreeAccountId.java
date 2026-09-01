package faang.school.accountservice.model;

import faang.school.accountservice.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite identifier for {@link FreeAccountNumber}.
 *
 * <p>Equality and hashing are restricted to the identifier fields only
 * ({@code type} + {@code accountNumber}) via explicit accessors instead of
 * Lombok's {@code @Data}, so that any future non-identifier field added to this
 * embeddable cannot silently corrupt JPA identity semantics (entity lookup,
 * merge, and cache keys all rely on identifier-safe equality).
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Embeddable
public class FreeAccountId {

    @EqualsAndHashCode.Include
    @Column(name = "type", nullable = false, length = 32)
    @Enumerated(value = EnumType.STRING)
    private AccountType type;

    @EqualsAndHashCode.Include
    @Column(name = "account_number", nullable = false)
    private long accountNumber;
}