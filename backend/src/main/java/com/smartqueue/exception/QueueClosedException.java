package com.smartqueue.exception;

public class QueueClosedException extends RuntimeException {
    public QueueClosedException(String message) {
        super(message);
    }
}
