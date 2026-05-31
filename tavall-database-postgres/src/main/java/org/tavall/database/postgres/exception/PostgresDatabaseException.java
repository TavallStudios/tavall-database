package org.tavall.database.postgres.exception;

public final class PostgresDatabaseException extends IllegalStateException {

    public PostgresDatabaseException(String message) {
        super(message);
    }

    public PostgresDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
