package faang.school.accountservice.config.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterLeaseTest {

    private static final String LEASE_KEY = "account-number-generate";

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ClusterLease clusterLease;
    private String holderId;

    @BeforeEach
    void setUp() {
        clusterLease = new ClusterLease(jdbcTemplate);
        holderId = (String) org.springframework.test.util.ReflectionTestUtils.getField(clusterLease, "holderId");
    }

    @Test
    void tryAcquire_whenClaimReturnsThisHolder_returnsTrue() {
        when(jdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class), eq(String.class)))
                .thenReturn(row(holderId));

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isTrue();
    }

    @Test
    void tryAcquire_whenClaimReturnsEmpty_returnsFalse() {
        when(jdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class), eq(String.class)))
                .thenReturn(emptyRows());

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isFalse();
    }

    @Test
    void tryAcquire_whenClaimReturnsOtherHolder_returnsFalse() {
        when(jdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class), eq(String.class)))
                .thenReturn(rowWithOtherHolder("other-node"));

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isFalse();
    }

    private List<String> row(String value) {
        return java.util.List.of(value);
    }

    private List<String> emptyRows() {
        return java.util.List.of();
    }

    private List<String> rowWithOtherHolder(String otherHolder) {
        return java.util.List.of(otherHolder);
    }

    @Test
    void tryAcquire_whenDatabaseUnavailable_failsOpen() {
        when(jdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class), eq(String.class)))
                .thenThrow(new org.springframework.dao.InvalidDataAccessApiUsageException("db down"));

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isTrue();
    }

    @Test
    void release_whenDatabaseUnavailable_swallowsException() {
        when(jdbcTemplate.update(anyString(), any(SqlParameterSource.class)))
                .thenThrow(new org.springframework.dao.InvalidDataAccessApiUsageException("db down"));

        assertThatCode(() -> clusterLease.release(LEASE_KEY)).doesNotThrowAnyException();
        verify(jdbcTemplate).update(anyString(), any(SqlParameterSource.class));
    }

    @Test
    void release_whenDatabaseAvailable_executesDelete() {
        when(jdbcTemplate.update(anyString(), any(SqlParameterSource.class))).thenReturn(1);

        assertThatCode(() -> clusterLease.release(LEASE_KEY)).doesNotThrowAnyException();
        verify(jdbcTemplate).update(anyString(), any(SqlParameterSource.class));
    }
}
