package faang.school.accountservice.service;

import faang.school.accountservice.enums.BalanceAuditOutcome;
import faang.school.accountservice.mapper.BalanceMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.model.Balance;
import faang.school.accountservice.model.BalanceAudit;
import faang.school.accountservice.repository.BalanceAuditRepository;
import faang.school.accountservice.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceAuditServiceTest {

    private static final long ACCOUNT_ID = 1L;
    private static final UUID TRANSACTION_ID = UUID.fromString("019535d9-3df7-79fb-b466-fa907fa17f9e");
    private static final String OPERATION = "creditBalance";

    @Mock
    private BalanceAuditRepository balanceAuditRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private BalanceMapper balanceMapper;

    @InjectMocks
    private BalanceAuditService balanceAuditService;

    private Balance balance;
    private BalanceAudit audit;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setId(ACCOUNT_ID);

        balance = new Balance();
        balance.setAccount(account);
        balance.setActualBalance(new BigDecimal("100.00"));
        balance.setAuthorizedBalance(BigDecimal.ZERO);

        audit = new BalanceAudit();
    }

    @Test
    void createSuccessfulAudit_setsSuccessOutcomeAndPersists() {
        when(balanceMapper.toBalanceAudit(balance)).thenReturn(audit);
        when(balanceAuditRepository.save(any(BalanceAudit.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceAudit saved = balanceAuditService.createSuccessfulAudit(balance, TRANSACTION_ID, OPERATION);

        assertThat(saved).isSameAs(audit);
        assertThat(saved.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(saved.getOperation()).isEqualTo(OPERATION);
        assertThat(saved.getOutcome()).isEqualTo(BalanceAuditOutcome.SUCCESS);
        verify(balanceAuditRepository).save(audit);
    }

    @Test
    void createFailedAudit_whenBalanceExists_setsFailedOutcomeAndPersists() {
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(balance);
        when(balanceMapper.toBalanceAudit(balance)).thenReturn(audit);
        when(balanceAuditRepository.save(any(BalanceAudit.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceAudit saved = balanceAuditService.createFailedAudit(ACCOUNT_ID, TRANSACTION_ID, OPERATION, "boom");

        assertThat(saved).isSameAs(audit);
        assertThat(saved.getOutcome()).isEqualTo(BalanceAuditOutcome.FAILED);
        assertThat(saved.getFailureReason()).isEqualTo("boom");
        verify(balanceAuditRepository).save(audit);
    }

    @Test
    void createFailedAudit_whenBalanceMissing_returnsNullWithoutSaving() {
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(null);

        BalanceAudit saved = balanceAuditService.createFailedAudit(ACCOUNT_ID, TRANSACTION_ID, OPERATION, "boom");

        assertThat(saved).isNull();
        verify(balanceAuditRepository, never()).save(any());
    }

    @Test
    void createFailedAudit_truncatesFailureReasonTo255Characters() {
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(balance);
        when(balanceMapper.toBalanceAudit(balance)).thenReturn(audit);
        when(balanceAuditRepository.save(any(BalanceAudit.class))).thenAnswer(inv -> inv.getArgument(0));

        String longReason = "x".repeat(300);
        BalanceAudit saved = balanceAuditService.createFailedAudit(ACCOUNT_ID, TRANSACTION_ID, OPERATION, longReason);

        assertThat(saved.getFailureReason()).hasSize(255);
    }

    @Test
    void createFailedAudit_keepsShortFailureReasonUnchanged() {
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(balance);
        when(balanceMapper.toBalanceAudit(balance)).thenReturn(audit);
        when(balanceAuditRepository.save(any(BalanceAudit.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceAudit saved = balanceAuditService.createFailedAudit(ACCOUNT_ID, TRANSACTION_ID, OPERATION, "short");

        assertThat(saved.getFailureReason()).isEqualTo("short");
    }

    @Test
    void createFailedAudit_keepsNullFailureReasonUnchanged() {
        when(balanceRepository.findByAccountId(ACCOUNT_ID)).thenReturn(balance);
        when(balanceMapper.toBalanceAudit(balance)).thenReturn(audit);
        when(balanceAuditRepository.save(any(BalanceAudit.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceAudit saved = balanceAuditService.createFailedAudit(ACCOUNT_ID, TRANSACTION_ID, OPERATION, null);

        assertThat(saved.getFailureReason()).isNull();
    }
}
