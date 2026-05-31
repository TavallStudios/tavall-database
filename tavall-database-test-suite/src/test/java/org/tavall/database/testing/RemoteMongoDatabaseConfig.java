package org.tavall.database.testing;

import com.mongodb.ConnectionString;
import org.junit.jupiter.api.Assumptions;

public record RemoteMongoDatabaseConfig(
        String host,
        int port,
        String username,
        String password,
        boolean readOnly,
        String databaseName,
        String collectionName,
        String documentId,
        String documentUsername
) {

    public static RemoteMongoDatabaseConfig fromEnv() {
        String mongoUri = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_MONGO_URI",
                "MONGO_URI",
                "MONGODB_URI",
                "MDB_MCP_CONNECTION_STRING"
        ).orElse("");

        String host;
        int port;
        String username;
        String password;
        String databaseName;

        if (!mongoUri.isBlank()) {
            Assumptions.assumeTrue(
                    !mongoUri.startsWith("mongodb+srv://"),
                    "MongoDB SRV URLs are not supported by the tavall-database-mongo test path."
            );

            ConnectionString connectionString = new ConnectionString(mongoUri);
            String hostPort = connectionString.getHosts().get(0);
            String[] hostParts = hostPort.split(":", 2);
            host = hostParts[0];
            port = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 27017;
            username = connectionString.getUsername() == null ? "" : connectionString.getUsername();
            char[] passwordCharacters = connectionString.getPassword();
            password = passwordCharacters == null ? "" : new String(passwordCharacters);
            databaseName = connectionString.getDatabase();
            if (databaseName == null || databaseName.isBlank()) {
                databaseName = "tavall";
            }
        } else {
            host = RemoteDatabaseEnvironment.requiredValue(
                    "Remote Mongo host",
                    "TAVALL_MONGO_HOST",
                    "MONGO_HOST"
            );
            port = RemoteDatabaseEnvironment.intOrDefault(
                    27017,
                    "TAVALL_MONGO_PORT",
                    "MONGO_PORT"
            );
            username = RemoteDatabaseEnvironment.firstValue(
                    "TAVALL_MONGO_USER",
                    "MONGO_USER"
            ).orElse("");
            password = RemoteDatabaseEnvironment.firstValue(
                    "TAVALL_MONGO_PASSWORD",
                    "MONGO_PASSWORD"
            ).orElse("");
            databaseName = RemoteDatabaseEnvironment.firstValue(
                    "TAVALL_MONGO_DATABASE",
                    "TAVALL_MONGO_DB",
                    "MONGO_DATABASE",
                    "MONGO_DB"
            ).orElse("tavall");
        }

        boolean readOnly = RemoteDatabaseEnvironment.booleanOrDefault(
                false,
                "TAVALL_MONGO_READ_ONLY",
                "MONGO_READ_ONLY"
        );
        String collectionName = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_MONGO_TEST_COLLECTION",
                "MONGO_TEST_COLLECTION"
        ).orElse("tavall_database_test_collection");
        String documentId = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_MONGO_TEST_DOCUMENT_ID",
                "MONGO_TEST_DOCUMENT_ID"
        ).orElse("remote-document-0001");
        String documentUsername = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_MONGO_TEST_DOCUMENT_USERNAME",
                "MONGO_TEST_DOCUMENT_USERNAME"
        ).orElse("remote-mongo-user");

        return new RemoteMongoDatabaseConfig(
                host,
                port,
                username,
                password,
                readOnly,
                databaseName,
                collectionName,
                documentId,
                documentUsername
        );
    }
}
