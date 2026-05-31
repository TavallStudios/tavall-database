package org.tavall.database.redis.exception;

public class RedisDatabaseException extends RuntimeException {

    public RedisDatabaseException(String message) {
        super(message);
    }

    public RedisDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
