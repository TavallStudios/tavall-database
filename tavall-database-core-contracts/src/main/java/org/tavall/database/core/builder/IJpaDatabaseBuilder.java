package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

public interface IJpaDatabaseBuilder<D extends IDatabase, B extends IJpaDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    B persistenceUnitName(String persistenceUnitName);

    B entityPackage(String entityPackage);

    B generateSchema(boolean generateSchema);

    B showSql(boolean showSql);
}

