package org.tavall.database.postgres;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseConfigData;
import org.tavall.database.core.database.IDatabaseType;

import java.util.List;

public interface IPostgresConfigData extends IDatabaseConfigData {

    @Override
    IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> getDatabaseType();

    @Override
    DatabaseConfigType getConfigType();

    @Override
    boolean isReadOnly();

    String getJdbcUrl();

    String getUsername();

    String getPassword();

    String getPersistenceUnitName();

    List<String> getEntityPackages();

    boolean shouldGenerateSchema();

    boolean shouldShowSql();
}
