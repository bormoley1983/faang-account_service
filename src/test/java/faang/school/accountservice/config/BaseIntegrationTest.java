package faang.school.accountservice.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public abstract class BaseIntegrationTest {
   
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:8-alpine");

    protected static final PostgreSQLContainer POSTGRESQL_CONTAINER;
    protected static final RedisContainer REDIS_CONTAINER;

    static {
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

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }
}
