package org.tavall.database.postgres.connection;

import org.postgresql.ds.PGSimpleDataSource;
import org.tavall.database.postgres.IPostgresConfigData;
import org.tavall.database.postgres.exception.PostgresConnectionException;
import org.tavall.logging.Log;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public final class PostgresConnectionHandler implements IPostgresConnectionHandler {

    private final IPostgresConfigData configData;
    private final PGSimpleDataSource dataSource;

    public PostgresConnectionHandler(IPostgresConfigData configData) {
        this.configData = configData;
        this.dataSource = new PGSimpleDataSource();
        this.dataSource.setUrl(configData.getJdbcUrl());

        String username = configData.getUsername();
        if (username != null && !username.isBlank()) {
            this.dataSource.setUser(username);
        }

        String password = configData.getPassword();
        if (password != null) {
            this.dataSource.setPassword(password);
        }
    }

    @Override
    public Optional<Connection> openConnection() {
        try {
            Connection connection = dataSource.getConnection();
            try {
                connection.setReadOnly(configData.isReadOnly());
                return Optional.of(connection);
            } catch (SQLException exception) {
                closeConnection(connection);
                PostgresConnectionException postgresConnectionException = new PostgresConnectionException(
                        "Unable to configure PostgreSQL connection.",
                        exception
                );
                Log.exception(postgresConnectionException);
                return Optional.empty();
            }
        } catch (SQLException exception) {
            PostgresConnectionException postgresConnectionException = new PostgresConnectionException(
                    "Unable to open PostgreSQL connection.",
                    exception
            );
            Log.exception(postgresConnectionException);
            return Optional.empty();
        }
    }

    @Override
    public void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException exception) {
            PostgresConnectionException postgresConnectionException = new PostgresConnectionException(
                    "Unable to close PostgreSQL connection.",
                    exception
            );
            Log.exception(postgresConnectionException);
        }
    }

    @Override
    public boolean isAvailable() {
        Optional<Connection> connectionOptional = openConnection();
        if (connectionOptional.isEmpty()) {
            return false;
        }

        Connection connection = connectionOptional.get();
        try {
            return connection.isValid(2);
        } catch (SQLException exception) {
            PostgresConnectionException postgresConnectionException = new PostgresConnectionException(
                    "Unable to validate PostgreSQL connection.",
                    exception
            );
            Log.exception(postgresConnectionException);
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public void close() {
    }
}
