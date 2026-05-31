package org.tavall.database.testing;

import org.junit.jupiter.api.Assumptions;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public record RemoteQdrantDatabaseConfig(
        String host,
        int port,
        String username,
        String password,
        boolean readOnly,
        String collectionName,
        int vectorSize,
        String distance
) {

    public static RemoteQdrantDatabaseConfig fromEnv() {
        String qdrantUrl = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_QDRANT_URL",
                "QDRANT_URL"
        ).orElse("");

        String host;
        int port;
        String username;
        String password;

        if (!qdrantUrl.isBlank()) {
            URI qdrantUri = URI.create(qdrantUrl);
            String scheme = qdrantUri.getScheme();
            Assumptions.assumeTrue(
                    !"https".equalsIgnoreCase(scheme),
                    "Qdrant HTTPS URLs are not supported by the tavall-database-qdrant test path."
            );

            host = qdrantUri.getHost();
            port = qdrantUri.getPort() > 0 ? qdrantUri.getPort() : 6333;
            String userInfo = qdrantUri.getUserInfo();
            username = extractUsername(userInfo);
            password = extractPassword(userInfo);
        } else {
            host = RemoteDatabaseEnvironment.requiredValue(
                    "Remote Qdrant host",
                    "TAVALL_QDRANT_HOST",
                    "QDRANT_HOST"
            );
            port = RemoteDatabaseEnvironment.intOrDefault(
                    6333,
                    "TAVALL_QDRANT_PORT",
                    "QDRANT_PORT"
            );
            username = RemoteDatabaseEnvironment.firstValue(
                    "TAVALL_QDRANT_USERNAME",
                    "QDRANT_USERNAME"
            ).orElse("");
            password = RemoteDatabaseEnvironment.firstValue(
                    "TAVALL_QDRANT_PASSWORD",
                    "QDRANT_PASSWORD"
            ).orElse("");
        }

        boolean readOnly = RemoteDatabaseEnvironment.booleanOrDefault(
                false,
                "TAVALL_QDRANT_READ_ONLY",
                "QDRANT_READ_ONLY"
        );
        String collectionName = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_QDRANT_TEST_COLLECTION",
                "QDRANT_TEST_COLLECTION"
        ).orElse("tavall_database_test_collection");
        int vectorSize = RemoteDatabaseEnvironment.intOrDefault(
                4,
                "TAVALL_QDRANT_VECTOR_SIZE",
                "QDRANT_VECTOR_SIZE"
        );
        String distance = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_QDRANT_DISTANCE",
                "QDRANT_DISTANCE"
        ).orElse("Cosine");

        return new RemoteQdrantDatabaseConfig(
                host,
                port,
                username,
                password,
                readOnly,
                collectionName,
                vectorSize,
                distance
        );
    }

    private static String extractUsername(String userInfo) {
        if (userInfo == null || userInfo.isBlank()) {
            return "";
        }

        String decodedUserInfo = URLDecoder.decode(userInfo, StandardCharsets.UTF_8);
        int separatorIndex = decodedUserInfo.indexOf(':');
        if (separatorIndex < 0) {
            return decodedUserInfo;
        }
        return decodedUserInfo.substring(0, separatorIndex);
    }

    private static String extractPassword(String userInfo) {
        if (userInfo == null || userInfo.isBlank()) {
            return "";
        }

        String decodedUserInfo = URLDecoder.decode(userInfo, StandardCharsets.UTF_8);
        int separatorIndex = decodedUserInfo.indexOf(':');
        if (separatorIndex < 0) {
            return "";
        }
        return decodedUserInfo.substring(separatorIndex + 1);
    }
}
