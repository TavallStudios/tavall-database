package org.tavall.database.mongo.exception;

public class MongoConnectionException extends RuntimeException {

    public MongoConnectionException(String message) {
        super(message);
    }

    public MongoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
