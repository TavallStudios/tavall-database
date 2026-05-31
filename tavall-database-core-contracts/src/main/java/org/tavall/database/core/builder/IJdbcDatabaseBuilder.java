package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

public interface IJdbcDatabaseBuilder<D extends IDatabase, B extends IJdbcDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    B jdbcUrl(String jdbcUrl);

    B username(String username);

    B password(String password);

    B readOnly(boolean readOnly);
}
