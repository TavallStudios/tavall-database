package org.tavall.database.redis.exception;

public class RedisQueryException extends RuntimeException {

    public RedisQueryException(String message) {
        super(message);
    }

    public RedisQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}

