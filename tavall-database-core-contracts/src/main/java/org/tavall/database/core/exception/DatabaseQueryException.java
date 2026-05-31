package org.tavall.database.core.exception;

public final class DatabaseQueryException extends IllegalStateException {

    public DatabaseQueryException(String message) {
        super(message);
    }

    public DatabaseQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
