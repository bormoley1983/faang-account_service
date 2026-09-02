package faang.school.accountservice.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.atomic.AtomicLong;

@Execution(ExecutionMode.SAME_THREAD)
public abstract class BaseIntegrationTest {

    private static final boolean CI_INTEGRATION =
            Boolean.parseBoolean(System.getenv("FAANG_CI_INTEGRATION"));

    private static final AtomicLong ACCOUNT_NUMBER_SEQUENCE =
            new AtomicLong(9_000_000_000_000_000L);
   
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:8-alpine");

    protected static final PostgreSQLContainer POSTGRESQL_CONTAINER;
    protected static final RedisContainer REDIS_CONTAINER;

    static {
        if (CI_INTEGRATION) {
            POSTGRESQL_CONTAINER = null;
            REDIS_CONTAINER = null;
        } else {
            Network testNetwork = Network.newNetwork();
            POSTGRESQL_CONTAINER = new PostgreSQLContainer(POSTGRES_IMAGE)
                    .withNetwork(testNetwork)
                    .withNetworkAliases("test-postgres")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            POSTGRESQL_CONTAINER.start();

            REDIS_CONTAINER = new RedisContainer(REDIS_IMAGE)
                    .withNetwork(testNetwork)
                    .withNetworkAliases("test-redis");
            REDIS_CONTAINER.start();
        }
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        if (CI_INTEGRATION) {
            registry.add("spring.datasource.url", () -> requiredEnvironment("FAANG_TEST_POSTGRES_URL"));
            registry.add("spring.datasource.username", () -> requiredEnvironment("FAANG_TEST_POSTGRES_USER"));
            registry.add("spring.datasource.password", () -> environment("FAANG_TEST_POSTGRES_PASSWORD", ""));
            registry.add("spring.data.redis.host", () -> requiredEnvironment("FAANG_TEST_REDIS_HOST"));
            registry.add("spring.data.redis.port",
                    () -> Integer.parseInt(requiredEnvironment("FAANG_TEST_REDIS_PORT")));
        } else {
            registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
            registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
            registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
            registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required CI integration setting: " + name);
        }
        return value;
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

    protected static String nextValidAccountNumber() {
        return Long.toString(ACCOUNT_NUMBER_SEQUENCE.incrementAndGet());
    }
}
