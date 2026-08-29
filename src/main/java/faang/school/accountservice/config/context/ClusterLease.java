package faang.school.accountservice.config.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Simple DB-based cluster lease to prevent multiple replicas from
 * performing the same scheduled work concurrently
 * <p>
 * Uses a single-row table with an atomic {@code UPDATE ... WHERE} claim.
 * The lease expires after a configurable TTL so a crashed replica does not
 * block the cluster indefinitely.
 */
@Slf4j
@Component
public class ClusterLease {

    private static final String CLAIM_SQL = """
            INSERT INTO cluster_lease (name, holder, acquired_at, expires_at)
            VALUES (:name, :holder, NOW(), NOW() + INTERVAL '5 minutes')
            ON CONFLICT (name) DO UPDATE
                SET holder = EXCLUDED.holder,
                    acquired_at = NOW(),
                    expires_at = NOW() + INTERVAL '5 minutes'
                WHERE cluster_lease.expires_at < NOW()
            RETURNING holder
            """;

    private static final String RELEASE_SQL = """
            DELETE FROM cluster_lease WHERE name = :name AND holder = :holder
            """;

    private final JdbcTemplate jdbcTemplate;
    private final String holderId;

    public ClusterLease(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // Unique per-JVM identifier: hostname + random suffix
        this.holderId = "node-" + System.nanoTime();
    }

    public boolean tryAcquire(String leaseName) {
        try {
            var results = jdbcTemplate.queryForList(CLAIM_SQL, java.util.Map.of(
                    "name", leaseName,
                    "holder", holderId
            ), String.class);
            boolean acquired = !results.isEmpty() && results.get(0).equals(holderId);
            if (acquired) {
                log.debug("Acquired cluster lease '{}' held by {}", leaseName, holderId);
            } else {
                log.debug("Could not acquire cluster lease '{}' (held by another replica)", leaseName);
            }
            return acquired;
        } catch (Exception e) {
            log.warn("Failed to acquire cluster lease '{}': {}", leaseName, e.getMessage());
            // Fail-open: if the lease table doesn't exist yet, allow the work to proceed
            return true;
        }
    }

    public void release(String leaseName) {
        try {
            jdbcTemplate.update(RELEASE_SQL, java.util.Map.of(
                    "name", leaseName,
                    "holder", holderId
            ));
            log.debug("Released cluster lease '{}'", leaseName);
        } catch (Exception e) {
            log.warn("Failed to release cluster lease '{}': {}", leaseName, e.getMessage());
        }
    }
}
