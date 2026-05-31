package org.tavall.database.mongo;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.tavall.database.DatabaseType;
import org.tavall.database.core.database.DatabaseBuilder;
import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.testing.RemoteMongoDatabaseConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MongoDatabaseSmokeTest {

    @Test
    void connectsToRemoteMongoAndRoundTripsADocument() {
        RemoteMongoDatabaseConfig config = RemoteMongoDatabaseConfig.fromEnv();

        Optional<IMongoDatabase> databaseOptional = DatabaseBuilder.create()
                .databaseType(DatabaseType.MONGO)
                .host(config.host())
                .port(config.port())
                .username(config.username())
                .password(config.password())
                .readOnly(config.readOnly())
                .databaseName(config.databaseName())
                .build();

        assertTrue(databaseOptional.isPresent());

        IMongoDatabase database = databaseOptional.get();
        MongoCollection<Document> collection = null;
        try {
            IMongoConfigData configData = database.getConfigData();

            assertEquals(DatabaseType.MONGO, database.getDatabaseType());
            assertEquals(DatabaseType.MONGO, configData.getDatabaseType());
            assertEquals(DatabaseConfigType.HOST_PORT, configData.getConfigType());
            assertEquals(config.host(), configData.getHost());
            assertEquals(config.port(), configData.getPort());
            assertEquals(config.username(), configData.getUsername());
            assertEquals(config.password(), configData.getPassword());
            assertEquals(config.readOnly(), configData.isReadOnly());
            assertEquals(config.databaseName(), configData.getDatabaseName());

            Assumptions.assumeTrue(
                    database.isAvailable(),
                    "Remote Mongo database is not reachable."
            );

            collection = database.connections()
                    .openDatabase()
                    .orElseThrow()
                    .getCollection(config.collectionName());

            collection.drop();

            Document document = new Document("_id", config.documentId())
                    .append("username", config.documentUsername())
                    .append("collection", config.collectionName());

            collection.insertOne(document);

            Document loadedDocument = collection.find(new Document("_id", config.documentId())).first();
            assertNotNull(loadedDocument);
            assertEquals(config.documentUsername(), loadedDocument.getString("username"));
            assertEquals(config.collectionName(), loadedDocument.getString("collection"));
        } finally {
            if (collection != null) {
                collection.drop();
            }
            database.close();
        }
    }
}
