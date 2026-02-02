package faang.school.accountservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "balance_audit")
@Entity
public class BalanceAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "authorized_balance", nullable = false)
    private BigDecimal authorizedBalance;

    @Column(name = "actual_balance", nullable = false)
    private BigDecimal actualBalance;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "balance_id", nullable = false)
    private Long balanceId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
