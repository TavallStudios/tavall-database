package org.tavall.database.postgres.connection;

import java.sql.Connection;
import java.util.Optional;

public interface IPostgresConnectionHandler extends AutoCloseable {

    Optional<Connection> openConnection();

    void closeConnection(Connection connection);

    boolean isAvailable();

    @Override
    void close();
}

