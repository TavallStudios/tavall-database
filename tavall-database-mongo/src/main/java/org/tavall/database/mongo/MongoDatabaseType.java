package org.tavall.database.mongo;

import org.tavall.database.core.database.IDatabaseType;

public final class MongoDatabaseType implements IDatabaseType<IMongoDatabase, IMongoDatabaseBuilder> {

    public static final MongoDatabaseType MONGO = new MongoDatabaseType();

    private MongoDatabaseType() {
    }

    @Override
    public String getTypeId() {
        return "mongo";
    }

    @Override
    public IMongoDatabaseBuilder createBuilder() {
        return MongoDatabaseBuilder.create();
    }
}
