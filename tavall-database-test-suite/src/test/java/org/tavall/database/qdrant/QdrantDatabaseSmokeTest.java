package org.tavall.database.qdrant;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.tavall.database.DatabaseType;
import org.tavall.database.core.database.DatabaseBuilder;
import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.testing.RemoteQdrantDatabaseConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QdrantDatabaseSmokeTest {

    @Test
    void connectsToRemoteQdrantAndManagesACollection() throws IOException, InterruptedException {
        RemoteQdrantDatabaseConfig config = RemoteQdrantDatabaseConfig.fromEnv();

        Optional<IQdrantDatabase> databaseOptional = DatabaseBuilder.create()
                .databaseType(DatabaseType.QDRANT)
                .host(config.host())
                .port(config.port())
                .username(config.username())
                .password(config.password())
                .readOnly(config.readOnly())
                .collectionName(config.collectionName())
                .build();

        assertTrue(databaseOptional.isPresent());

        IQdrantDatabase database = databaseOptional.get();
        HttpClient client = null;
        try {
            IQdrantConfigData configData = database.getConfigData();

            assertEquals(DatabaseType.QDRANT, database.getDatabaseType());
            assertEquals(DatabaseType.QDRANT, configData.getDatabaseType());
            assertEquals(DatabaseConfigType.HOST_PORT, configData.getConfigType());
            assertEquals(config.host(), configData.getHost());
            assertEquals(config.port(), configData.getPort());
            assertEquals(config.username(), configData.getUsername());
            assertEquals(config.password(), configData.getPassword());
            assertEquals(config.readOnly(), configData.isReadOnly());
            assertEquals(config.collectionName(), configData.getCollectionName());

            Assumptions.assumeTrue(
                    database.isAvailable(),
                    "Remote Qdrant database is not reachable."
            );

            client = database.connections()
                    .openClient()
                    .orElseThrow();

            URI baseUri = URI.create("http://" + config.host() + ":" + config.port());

            HttpRequest deleteExistingCollectionRequest = HttpRequest.newBuilder(
                            baseUri.resolve("/collections/" + config.collectionName())
                    )
                    .DELETE()
                    .build();
            client.send(deleteExistingCollectionRequest, HttpResponse.BodyHandlers.ofString());

            HttpRequest createCollectionRequest = HttpRequest.newBuilder(
                            baseUri.resolve("/collections/" + config.collectionName())
                    )
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString("""
                            {
                              "vectors": {
                                "size": %d,
                                "distance": "%s"
                              }
                            }
                            """.formatted(config.vectorSize(), config.distance())))
                    .build();

            HttpResponse<String> createCollectionResponse = client.send(
                    createCollectionRequest,
                    HttpResponse.BodyHandlers.ofString()
            );
            assertTrue(createCollectionResponse.statusCode() >= 200 && createCollectionResponse.statusCode() < 300);
            assertNotNull(createCollectionResponse.body());

            HttpRequest getCollectionRequest = HttpRequest.newBuilder(
                            baseUri.resolve("/collections/" + config.collectionName())
                    )
                    .GET()
                    .build();

            HttpResponse<String> getCollectionResponse = client.send(
                    getCollectionRequest,
                    HttpResponse.BodyHandlers.ofString()
            );
            assertTrue(getCollectionResponse.statusCode() >= 200 && getCollectionResponse.statusCode() < 300);
            assertTrue(getCollectionResponse.body().contains(config.collectionName()));
        } finally {
            if (client != null) {
                try {
                    URI baseUri = URI.create("http://" + config.host() + ":" + config.port());
                    HttpRequest deleteCollectionRequest = HttpRequest.newBuilder(
                                    baseUri.resolve("/collections/" + config.collectionName())
                            )
                            .DELETE()
                            .build();
                    client.send(deleteCollectionRequest, HttpResponse.BodyHandlers.ofString());
                } catch (IOException exception) {
                    // Ignore cleanup failures in the remote test path.
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
            database.close();
        }
    }
}

