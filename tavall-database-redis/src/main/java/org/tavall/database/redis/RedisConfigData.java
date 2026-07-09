package org.tavall.database.redis;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseType;

public final class RedisConfigData implements IRedisConfigData {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean readOnly;
    private final int databaseIndex;

    public RedisConfigData(
            String host,
            int port,
            String username,
            String password,
            boolean readOnly,
            int databaseIndex
    ) {
        this.host = normalizeText(host);
        this.port = port;
        this.username = normalizeText(username);
        this.password = normalizeText(password);
        this.readOnly = readOnly;
        this.databaseIndex = databaseIndex;
    }

    @Override
    public IDatabaseType<IRedisDatabase, IRedisDatabaseBuilder> getDatabaseType() {
        return RedisDatabaseType.REDIS;
    }

    @Override
    public DatabaseConfigType getConfigType() {
        return DatabaseConfigType.HOST_PORT;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getDatabaseIndex() {
        return databaseIndex;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}

