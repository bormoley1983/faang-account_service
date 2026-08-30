package faang.school.accountservice.config.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterLeaseTest {

    private static final String LEASE_KEY = "account-number-generate";

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ClusterLease clusterLease;
    private String holderId;

    @BeforeEach
    void setUp() {
        clusterLease = new ClusterLease(jdbcTemplate);
        holderId = (String) org.springframework.test.util.ReflectionTestUtils.getField(clusterLease, "holderId");
    }

    @Test
    void tryAcquire_whenClaimReturnsThisHolder_returnsTrue() {
        when(jdbcTemplate.queryForList(anyString(), anyMap(), eq(String.class)))
                .thenReturn(row(holderId));

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isTrue();
    }

    @Test
    void tryAcquire_whenClaimReturnsEmpty_returnsFalse() {
        when(jdbcTemplate.queryForList(anyString(), anyMap(), eq(String.class)))
                .thenReturn(emptyRows());

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isFalse();
    }

    @Test
    void tryAcquire_whenClaimReturnsOtherHolder_returnsFalse() {
        when(jdbcTemplate.queryForList(anyString(), anyMap(), eq(String.class)))
                .thenReturn(rowWithOtherHolder("other-node"));

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isFalse();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Map<String, Object>> row(String value) {
        // queryForList(sql, args, String.class) returns one element per column of the
        // first row; with a single-column RETURNING clause that is the raw value.
        List rows = new java.util.ArrayList();
        rows.add(value);
        return (List<Map<String, Object>>) (List<?>) rows;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Map<String, Object>> emptyRows() {
        return (List<Map<String, Object>>) (List) new java.util.ArrayList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Map<String, Object>> rowWithOtherHolder(String otherHolder) {
        // A Map row whose first value is the foreign holder: results.get(0).equals(holderId) is false.
        List rows = new java.util.ArrayList();
        rows.add(Map.of("holder", otherHolder));
        return (List<Map<String, Object>>) (List<?>) rows;
    }

    @Test
    void tryAcquire_whenDatabaseUnavailable_failsOpen() {
        when(jdbcTemplate.queryForList(anyString(), anyMap(), eq(String.class)))
                .thenThrow(new org.springframework.dao.InvalidDataAccessApiUsageException("db down"));

        assertThat(clusterLease.tryAcquire(LEASE_KEY)).isTrue();
    }

    @Test
    void release_whenDatabaseUnavailable_swallowsException() {
        when(jdbcTemplate.update(anyString(), anyMap()))
                .thenThrow(new org.springframework.dao.InvalidDataAccessApiUsageException("db down"));

        assertThatCode(() -> clusterLease.release(LEASE_KEY)).doesNotThrowAnyException();
        verify(jdbcTemplate).update(anyString(), anyMap());
    }

    @Test
    void release_whenDatabaseAvailable_executesDelete() {
        when(jdbcTemplate.update(anyString(), anyMap())).thenReturn(1);

        assertThatCode(() -> clusterLease.release(LEASE_KEY)).doesNotThrowAnyException();
        verify(jdbcTemplate).update(anyString(), anyMap());
    }
}
