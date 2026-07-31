package org.tavall.database.postgres;

import org.tavall.database.core.builder.IJdbcDatabaseBuilder;
import org.tavall.database.core.builder.IJpaDatabaseBuilder;

public interface IPostgresDatabaseBuilder
        extends IJdbcDatabaseBuilder<IPostgresDatabase, IPostgresDatabaseBuilder>,
                IJpaDatabaseBuilder<IPostgresDatabase, IPostgresDatabaseBuilder> {

    IPostgresDatabaseBuilder entityClass(Class<?> entityClass);

    IPostgresDatabaseBuilder entityClasses(Class<?>... entityClasses);

    IPostgresDatabaseBuilder entityClassLoader(ClassLoader entityClassLoader);
}
