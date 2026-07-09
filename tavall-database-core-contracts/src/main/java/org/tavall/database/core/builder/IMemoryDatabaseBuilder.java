package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

public interface IMemoryDatabaseBuilder<D extends IDatabase, B extends IMemoryDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    B databaseName(String databaseName);
}

