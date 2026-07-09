package org.tavall.database.qdrant.exception;

public class QdrantQueryException extends RuntimeException {

    public QdrantQueryException(String message) {
        super(message);
    }

    public QdrantQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}

