package com.smartqueue.exception;

public class ConcurrentQueueOperationException extends RuntimeException {
    public ConcurrentQueueOperationException(String message) {
        super(message);
    }
}
