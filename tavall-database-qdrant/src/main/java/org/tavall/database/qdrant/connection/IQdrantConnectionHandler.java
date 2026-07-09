package org.tavall.database.qdrant.connection;

import java.net.http.HttpClient;
import java.util.Optional;

public interface IQdrantConnectionHandler extends AutoCloseable {

    Optional<HttpClient> openClient();

    boolean isAvailable();

    @Override
    void close();
}

