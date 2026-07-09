package org.tavall.database.core.database;

public interface IDatabaseConfigData {

    IDatabaseType<?, ?> getDatabaseType();

    DatabaseConfigType getConfigType();

    boolean isReadOnly();
}

