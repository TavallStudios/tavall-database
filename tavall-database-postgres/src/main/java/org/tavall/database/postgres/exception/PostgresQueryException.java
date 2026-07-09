package org.tavall.database.postgres.exception;

public final class PostgresQueryException extends IllegalStateException {

    public PostgresQueryException(String message) {
        super(message);
    }

    public PostgresQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}

