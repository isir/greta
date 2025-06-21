package greta.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for integration tests providing containerized infrastructure
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {
    
    protected static final Network NETWORK = Network.newNetwork();
    
    // PostgreSQL container
    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("greta_test")
            .withUsername("greta")
            .withPassword("test123")
            .withNetwork(NETWORK)
            .withNetworkAliases("postgres")
            .withReuse(true);
    
    // Redis container
    @Container
    protected static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379)
            .withNetwork(NETWORK)
            .withNetworkAliases("redis")
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1))
            .withReuse(true);
    
    // ActiveMQ container
    @Container
    protected static final GenericContainer<?> activemq = new GenericContainer<>(
            DockerImageName.parse("rmohr/activemq:5.18.0-alpine"))
            .withExposedPorts(61616, 8161)
            .withNetwork(NETWORK)
            .withNetworkAliases("activemq")
            .waitingFor(Wait.forListeningPort())
            .withReuse(true);
    
    // Greta application container (built from our Dockerfile)
    protected GenericContainer<?> gretaApp;
    
    @BeforeAll
    public void setupContainers() {
        // Start infrastructure containers
        postgres.start();
        redis.start();
        activemq.start();
        
        // Initialize Greta application container with proper configuration
        gretaApp = createGretaContainer();
    }
    
    @BeforeEach
    public void resetTestEnvironment() {
        // Reset database state if needed
        // Clear caches if needed
    }
    
    /**
     * Creates and configures the Greta application container
     */
    protected GenericContainer<?> createGretaContainer() {
        Map<String, String> env = new HashMap<>();
        env.put("GRETA_ENV", "test");
        env.put("DATABASE_URL", String.format("jdbc:postgresql://postgres:5432/greta_test"));
        env.put("DATABASE_USERNAME", "greta");
        env.put("DATABASE_PASSWORD", "test123");
        env.put("REDIS_HOST", "redis");
        env.put("REDIS_PORT", "6379");
        env.put("ACTIVEMQ_URL", "tcp://activemq:61616");
        env.put("JAVA_OPTS", "-Xmx512m -Xms256m");
        
        return new GenericContainer<>(DockerImageName.parse("greta:latest"))
                .withNetwork(NETWORK)
                .withNetworkAliases("greta-app")
                .withExposedPorts(8080, 8081)
                .withEnv(env)
                .waitingFor(Wait.forHttp("/health")
                        .forPort(8080)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .withReuse(true);
    }
    
    /**
     * Get the base URL for the Greta application
     */
    protected String getGretaBaseUrl() {
        return String.format("http://localhost:%d", gretaApp.getMappedPort(8080));
    }
    
    /**
     * Get the management URL for the Greta application
     */
    protected String getGretaManagementUrl() {
        return String.format("http://localhost:%d", gretaApp.getMappedPort(8081));
    }
    
    /**
     * Get PostgreSQL JDBC URL
     */
    protected String getPostgresJdbcUrl() {
        return postgres.getJdbcUrl();
    }
    
    /**
     * Get Redis connection string
     */
    protected String getRedisUrl() {
        return String.format("redis://localhost:%d", redis.getMappedPort(6379));
    }
    
    /**
     * Get ActiveMQ connection string
     */
    protected String getActiveMQUrl() {
        return String.format("tcp://localhost:%d", activemq.getMappedPort(61616));
    }
}