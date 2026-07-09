package org.tavall.database.qdrant;

import org.tavall.database.core.database.IDatabaseType;

public final class QdrantDatabaseType implements IDatabaseType<IQdrantDatabase, IQdrantDatabaseBuilder> {

    public static final QdrantDatabaseType QDRANT = new QdrantDatabaseType();

    private QdrantDatabaseType() {
    }

    @Override
    public String getTypeId() {
        return "qdrant";
    }

    @Override
    public IQdrantDatabaseBuilder createBuilder() {
        return QdrantDatabaseBuilder.create();
    }
}

