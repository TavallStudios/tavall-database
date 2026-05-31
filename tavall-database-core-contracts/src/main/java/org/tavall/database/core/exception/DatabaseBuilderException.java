package org.tavall.database.core.exception;

public final class DatabaseBuilderException extends IllegalStateException {

    public DatabaseBuilderException(String message) {
        super(message);
    }

    public DatabaseBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
