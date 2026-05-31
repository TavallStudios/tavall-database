package org.tavall.database.core.database;

public interface IDatabaseType<D extends IDatabase, B extends IDatabaseBuilder<D>> {

    String getTypeId();

    B createBuilder();
}
