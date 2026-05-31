package org.tavall.database.core.database;

import org.tavall.database.core.exception.DatabaseBuilderException;
import org.tavall.logging.Log;

public final class DatabaseBuilder {

    private DatabaseBuilder() {
    }

    public static DatabaseBuilder create() {
        return new DatabaseBuilder();
    }

    public <D extends IDatabase, B extends IDatabaseBuilder<D>> B databaseType(IDatabaseType<D, B> databaseType) {
        if (databaseType == null) {
            DatabaseBuilderException exception = new DatabaseBuilderException(
                    "Unable to create database builder because database type is null."
            );
            Log.exception(exception);
            throw exception;
        }

        B databaseBuilder = databaseType.createBuilder();
        if (databaseBuilder == null) {
            DatabaseBuilderException exception = new DatabaseBuilderException(
                    "Unable to create database builder because the database type returned null for type id "
                            + databaseType.getTypeId() + "."
            );
            Log.exception(exception);
            throw exception;
        }

        return databaseBuilder;
    }
}
