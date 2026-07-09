package org.tavall.database.core.database;

import java.util.Optional;

public interface IDatabaseBuilder<D extends IDatabase> {

    Optional<D> build();
}

