package org.tavall.database.postgres.connection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;

public interface IPostgresConnectionHandler extends AutoCloseable {

    DataSource dataSource();

    Optional<Connection> openConnection();

    void closeConnection(Connection connection);

    boolean isAvailable();

    @Override
    void close();
}
