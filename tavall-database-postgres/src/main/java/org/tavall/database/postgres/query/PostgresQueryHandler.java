package org.tavall.database.postgres.query;

import org.tavall.database.core.query.IDatabaseResultMapper;
import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.exception.PostgresQueryException;
import org.tavall.logging.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresQueryHandler implements IPostgresQueryHandler {

    private final IPostgresConnectionHandler connectionHandler;

    public PostgresQueryHandler(IPostgresConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public boolean executePreparedStatement(String sql, Object... params) {
        int updateCount = executePreparedStatementAndCount(sql, params);
        return updateCount >= 0;
    }

    @Override
    public int executePreparedStatementAndCount(String sql, Object... params) {
        if (sql == null || sql.isBlank()) {
            PostgresQueryException exception = new PostgresQueryException(
                    "Unable to execute PostgreSQL prepared statement because sql is null or blank."
            );
            Log.exception(exception);
            return -1;
        }

        Log.info("Executing PostgreSQL prepared statement: " + normalizeSql(sql));

        Optional<Connection> connectionOptional = connectionHandler.openConnection();
        if (connectionOptional.isEmpty()) {
            return -1;
        }

        Connection connection = connectionOptional.get();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params);
            return preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            PostgresQueryException postgresQueryException = new PostgresQueryException(
                    "Unable to execute PostgreSQL prepared statement.",
                    exception
            );
            Log.exception(postgresQueryException);
            return -1;
        } finally {
            connectionHandler.closeConnection(connection);
        }
    }

    @Override
    public <T> Optional<T> queryOne(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        if (sql == null || sql.isBlank()) {
            PostgresQueryException exception = new PostgresQueryException(
                    "Unable to execute PostgreSQL query because sql is null or blank."
            );
            Log.exception(exception);
            return Optional.empty();
        }
        if (resultMapper == null) {
            PostgresQueryException exception = new PostgresQueryException(
                    "Unable to execute PostgreSQL query because result mapper is null."
            );
            Log.exception(exception);
            return Optional.empty();
        }

        Log.info("Executing PostgreSQL query one: " + normalizeSql(sql));

        Optional<Connection> connectionOptional = connectionHandler.openConnection();
        if (connectionOptional.isEmpty()) {
            return Optional.empty();
        }

        Connection connection = connectionOptional.get();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                T mappedValue = resultMapper.map(resultSet);
                return Optional.ofNullable(mappedValue);
            }
        } catch (SQLException exception) {
            PostgresQueryException postgresQueryException = new PostgresQueryException(
                    "Unable to execute PostgreSQL query.",
                    exception
            );
            Log.exception(postgresQueryException);
            return Optional.empty();
        } finally {
            connectionHandler.closeConnection(connection);
        }
    }

    @Override
    public <T> List<T> queryList(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        if (sql == null || sql.isBlank()) {
            PostgresQueryException exception = new PostgresQueryException(
                    "Unable to execute PostgreSQL query because sql is null or blank."
            );
            Log.exception(exception);
            return List.of();
        }
        if (resultMapper == null) {
            PostgresQueryException exception = new PostgresQueryException(
                    "Unable to execute PostgreSQL query because result mapper is null."
            );
            Log.exception(exception);
            return List.of();
        }

        Log.info("Executing PostgreSQL query list: " + normalizeSql(sql));

        Optional<Connection> connectionOptional = connectionHandler.openConnection();
        if (connectionOptional.isEmpty()) {
            return List.of();
        }

        Connection connection = connectionOptional.get();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    T mappedValue = resultMapper.map(resultSet);
                    results.add(mappedValue);
                }
                return results;
            }
        } catch (SQLException exception) {
            PostgresQueryException postgresQueryException = new PostgresQueryException(
                    "Unable to execute PostgreSQL query list.",
                    exception
            );
            Log.exception(postgresQueryException);
            return List.of();
        } finally {
            connectionHandler.closeConnection(connection);
        }
    }

    private void bindParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
        if (params == null) {
            return;
        }

        for (int index = 0; index < params.length; index++) {
            preparedStatement.setObject(index + 1, params[index]);
        }
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}

