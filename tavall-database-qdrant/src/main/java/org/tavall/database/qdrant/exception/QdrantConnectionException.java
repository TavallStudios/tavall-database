package org.tavall.database.qdrant.exception;

public class QdrantConnectionException extends RuntimeException {

    public QdrantConnectionException(String message) {
        super(message);
    }

    public QdrantConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
