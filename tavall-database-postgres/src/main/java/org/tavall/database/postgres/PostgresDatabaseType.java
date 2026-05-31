package org.tavall.database.postgres;

import org.tavall.database.core.database.IDatabaseType;

public final class PostgresDatabaseType implements IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> {

    public static final PostgresDatabaseType POSTGRES = new PostgresDatabaseType();

    private PostgresDatabaseType() {
    }

    @Override
    public String getTypeId() {
        return "postgres";
    }

    @Override
    public IPostgresDatabaseBuilder createBuilder() {
        return PostgresDatabaseBuilder.create();
    }
}
