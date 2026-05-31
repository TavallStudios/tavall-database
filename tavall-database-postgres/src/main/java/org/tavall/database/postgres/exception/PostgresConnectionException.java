package org.tavall.database.postgres.exception;

public final class PostgresConnectionException extends IllegalStateException {

    public PostgresConnectionException(String message) {
        super(message);
    }

    public PostgresConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
