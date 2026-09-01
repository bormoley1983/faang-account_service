package faang.school.accountservice.model;

import faang.school.accountservice.enums.BalanceAuditOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "balance_audit")
@Entity
public class BalanceAudit {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "authorized_balance", nullable = false)
    private BigDecimal authorizedBalance;

    @Column(name = "actual_balance", nullable = false)
    private BigDecimal actualBalance;

    @Column(name = "transaction_id", nullable = false, columnDefinition = "uuid")
    private UUID transactionId;

    @Column(name = "operation", nullable = false, length = 64)
    private String operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private BalanceAuditOutcome outcome;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "commit_status", length = 16)
    private String commitStatus;

    @Column(name = "balance_id", nullable = false)
    private Long balanceId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
