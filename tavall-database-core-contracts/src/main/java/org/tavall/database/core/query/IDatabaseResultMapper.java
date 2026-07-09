package org.tavall.database.core.query;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface IDatabaseResultMapper<T> {

    T map(ResultSet resultSet) throws SQLException;
}

