package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

public interface IHostPortDatabaseBuilder<D extends IDatabase, B extends IHostPortDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    B host(String host);

    B port(int port);

    B username(String username);

    B password(String password);

    B readOnly(boolean readOnly);
}
