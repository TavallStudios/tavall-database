package org.tavall.database.qdrant.exception;

public class QdrantDatabaseException extends RuntimeException {

    public QdrantDatabaseException(String message) {
        super(message);
    }

    public QdrantDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

