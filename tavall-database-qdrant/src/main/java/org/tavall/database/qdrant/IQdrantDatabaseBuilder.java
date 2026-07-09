package org.tavall.database.qdrant;

import org.tavall.database.core.builder.IHostPortDatabaseBuilder;

public interface IQdrantDatabaseBuilder
        extends IHostPortDatabaseBuilder<IQdrantDatabase, IQdrantDatabaseBuilder> {

    IQdrantDatabaseBuilder collectionName(String collectionName);
}

