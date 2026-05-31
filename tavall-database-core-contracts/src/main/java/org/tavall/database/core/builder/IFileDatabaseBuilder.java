package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

import java.nio.file.Path;

public interface IFileDatabaseBuilder<D extends IDatabase, B extends IFileDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    B filePath(Path filePath);

    B readOnly(boolean readOnly);
}
