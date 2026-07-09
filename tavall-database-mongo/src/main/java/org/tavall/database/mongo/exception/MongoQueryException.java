package org.tavall.database.mongo.exception;

public class MongoQueryException extends RuntimeException {

    public MongoQueryException(String message) {
        super(message);
    }

    public MongoQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}

