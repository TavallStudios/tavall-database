package org.tavall.database.testing;

import org.junit.jupiter.api.Assumptions;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public record RemoteRedisDatabaseConfig(
        String host,
        int port,
        String username,
        String password,
        boolean readOnly,
        int databaseIndex,
        String key,
        String value
) {

    public static RemoteRedisDatabaseConfig fromEnv() {
        String redisUrl = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_REDIS_URL",
                "REDIS_URL",
                "REDIS_MCP_URL"
        ).orElse("");

        String host;
        int port;
        String username;
        String password;
        int databaseIndex;

        if (!redisUrl.isBlank()) {
            URI redisUri = URI.create(redisUrl);
            String scheme = redisUri.getScheme();
            Assumptions.assumeTrue(
                    !"rediss".equalsIgnoreCase(scheme),
                    "Redis TLS URLs are not supported by the tavall-database-redis test path."
            );

            host = redisUri.getHost();
            port = redisUri.getPort() > 0 ? redisUri.getPort() : 6379;
            username = extractRedisUsername(redisUri.getUserInfo());
            password = extractRedisPassword(redisUri.getUserInfo());
            databaseIndex = extractRedisDatabaseIndex(redisUri.getPath());
        } else {
            host = RemoteDatabaseEnvironment.requiredValue(
                    "Remote Redis host",
                    "TAVALL_REDIS_HOST",
                    "REDIS_HOST"
            );
            port = RemoteDatabaseEnvironment.intOrDefault(
                    6379,
                    "TAVALL_REDIS_PORT",
                    "REDIS_PORT"
            );
            username = RemoteDatabaseEnvironment.firstValue(
                    "TAVALL_REDIS_USERNAME",
                    "REDIS_USERNAME"
            ).orElse("");
            password = RemoteDatabaseEnvironment.firstValue(
                    "TAVALL_REDIS_PASSWORD",
                    "REDIS_PASSWORD"
            ).orElse("");
            databaseIndex = RemoteDatabaseEnvironment.intOrDefault(
                    0,
                    "TAVALL_REDIS_DATABASE_INDEX",
                    "REDIS_DATABASE_INDEX"
            );
        }

        boolean readOnly = RemoteDatabaseEnvironment.booleanOrDefault(
                false,
                "TAVALL_REDIS_READ_ONLY",
                "REDIS_READ_ONLY"
        );
        String key = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_REDIS_TEST_KEY",
                "REDIS_TEST_KEY"
        ).orElse("tavall:database:test:redis");
        String value = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_REDIS_TEST_VALUE",
                "REDIS_TEST_VALUE"
        ).orElse("remote-redis-value");

        return new RemoteRedisDatabaseConfig(
                host,
                port,
                username,
                password,
                readOnly,
                databaseIndex,
                key,
                value
        );
    }

    private static String extractRedisUsername(String userInfo) {
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

    private static String extractRedisPassword(String userInfo) {
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

    private static int extractRedisDatabaseIndex(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return 0;
        }

        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        if (normalizedPath.isBlank()) {
            return 0;
        }

        return Integer.parseInt(normalizedPath);
    }
}

