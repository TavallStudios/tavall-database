package org.tavall.database.mongo;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseType;

public final class MongoConfigData implements IMongoConfigData {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean readOnly;
    private final String databaseName;

    public MongoConfigData(
            String host,
            int port,
            String username,
            String password,
            boolean readOnly,
            String databaseName
    ) {
        this.host = normalizeText(host);
        this.port = port;
        this.username = normalizeText(username);
        this.password = normalizeText(password);
        this.readOnly = readOnly;
        this.databaseName = normalizeText(databaseName);
    }

    @Override
    public IDatabaseType<IMongoDatabase, IMongoDatabaseBuilder> getDatabaseType() {
        return MongoDatabaseType.MONGO;
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
    public String getDatabaseName() {
        return databaseName;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
