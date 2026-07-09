package org.tavall.database.mongo.exception;

public class MongoDatabaseException extends RuntimeException {

    public MongoDatabaseException(String message) {
        super(message);
    }

    public MongoDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

