package org.example.orderserver.exception;

public class InconsistentDataException extends RuntimeException {
    public InconsistentDataException(String message) {
        super(message);
    }
}
