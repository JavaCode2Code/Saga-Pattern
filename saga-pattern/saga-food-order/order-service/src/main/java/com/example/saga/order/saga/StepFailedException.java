package com.example.saga.order.saga;

public class StepFailedException extends RuntimeException {
    public StepFailedException(String message) { super(message); }
}
