package org.tavall.database.qdrant.connection;

import org.tavall.database.qdrant.IQdrantConfigData;
import org.tavall.database.qdrant.exception.QdrantConnectionException;
import org.tavall.logging.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public final class QdrantConnectionHandler implements IQdrantConnectionHandler {

    private final HttpClient client;
    private final URI readyUri;
    private boolean closed;

    public QdrantConnectionHandler(IQdrantConfigData configData) {
        this.client = HttpClient.newHttpClient();
        this.readyUri = URI.create(buildBaseUrl(configData) + "/readyz");
    }

    @Override
    public Optional<HttpClient> openClient() {
        if (closed) {
            return Optional.empty();
        }
        return Optional.of(client);
    }

    @Override
    public boolean isAvailable() {
        if (closed) {
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder(readyUri)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            QdrantConnectionException qdrantConnectionException = new QdrantConnectionException(
                    "Unable to validate Qdrant connection.",
                    exception
            );
            Log.exception(qdrantConnectionException);
            return false;
        }
    }

    @Override
    public void close() {
        closed = true;
    }

    private String buildBaseUrl(IQdrantConfigData configData) {
        return "http://" + configData.getHost() + ":" + configData.getPort();
    }
}
