package pe.ask.persistence.core.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * Configuration class for creating the PostgreSQL R2DBC Connection Pool.
 * <p>
 * This auto-configuration is triggered before the default {@link org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration}
 * and sets up a customized connection pool using the provided {@link PostgresqlConnectionProperties}.
 * </p>
 *
 * @author Allan Sagastegui
 */
@AutoConfiguration(before = R2dbcAutoConfiguration.class)
@ConditionalOnClass({ConnectionPool.class, PostgresqlConnectionFactory.class})
@ConditionalOnProperty(prefix = "pe.ask.persistence", name = {"host", "database", "username"})
@EnableConfigurationProperties(PostgresqlConnectionProperties.class)
public class PostgreSQLConnectionPool {

    /**
     * Default constructor for PostgreSQLConnectionPool.
     */
    public PostgreSQLConnectionPool() {
    }

    /**
     * The initial size of the connection pool.
     */
    public static final int INITIAL_SIZE = 12;

    /**
     * The maximum size of the connection pool.
     */
    public static final int MAX_SIZE = 15;

    /**
     * The maximum idle time for a connection in the pool (in minutes).
     */
    public static final int MAX_IDLE_TIME = 30;

    /**
     * Creates the connection factory bean for the PostgreSQL connection pool.
     *
     * @param properties the connection properties
     * @return the configured connection factory
     */
    @Bean
    @ConditionalOnMissingBean(ConnectionFactory.class)
    public ConnectionFactory connectionFactory(PostgresqlConnectionProperties properties) {
        PostgresqlConnectionConfiguration dbConfiguration = PostgresqlConnectionConfiguration.builder()
                .host(properties.host())
                .port(properties.port())
                .database(properties.database())
                .schema(properties.schema())
                .username(properties.username())
                .password(properties.password())
                .build();

        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder()
                .connectionFactory(new PostgresqlConnectionFactory(dbConfiguration))
                .name("api-postgres-connection-pool")
                .initialSize(INITIAL_SIZE)
                .maxSize(MAX_SIZE)
                .maxIdleTime(Duration.ofMinutes(MAX_IDLE_TIME))
                .validationQuery("SELECT 1")
                .build();

        return new ConnectionPool(poolConfiguration);
    }
}