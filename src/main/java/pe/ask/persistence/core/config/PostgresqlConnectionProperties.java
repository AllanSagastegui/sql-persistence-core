package pe.ask.persistence.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for PostgreSQL database connection.
 * <p>
 * Binds properties with the prefix "pe.ask.persistence" to configure the host, port, database,
 * schema, username, and password for the connection pool.
 * </p>
 *
 * @param host     the database host
 * @param port     the database port
 * @param database the database name
 * @param schema   the database schema
 * @param username the database username
 * @param password the database password
 *
 * @author Allan Sagastegui
 */
@ConfigurationProperties(prefix = "pe.ask.persistence")
public record PostgresqlConnectionProperties(
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password
) { }