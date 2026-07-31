package org.tavall.database.postgres.exception;

public final class PostgresJpaException extends IllegalStateException {

    public PostgresJpaException(String message) {
        super(message);
    }

    public PostgresJpaException(String message, Throwable cause) {
        super(message, cause);
    }
}
