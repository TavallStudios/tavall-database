package org.tavall.database.qdrant;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseType;

public final class QdrantConfigData implements IQdrantConfigData {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean readOnly;
    private final String collectionName;

    public QdrantConfigData(
            String host,
            int port,
            String username,
            String password,
            boolean readOnly,
            String collectionName
    ) {
        this.host = normalizeText(host);
        this.port = port;
        this.username = normalizeText(username);
        this.password = normalizeText(password);
        this.readOnly = readOnly;
        this.collectionName = normalizeText(collectionName);
    }

    @Override
    public IDatabaseType<IQdrantDatabase, IQdrantDatabaseBuilder> getDatabaseType() {
        return QdrantDatabaseType.QDRANT;
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
    public String getCollectionName() {
        return collectionName;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}

