package com.example.saga.order;

public enum OrderStatus {
    /** Saga running (or interrupted; the recovery job will compensate it). */
    PENDING,
    /** Every step succeeded. */
    CONFIRMED,
    /** A step failed and every completed step was compensated. */
    CANCELLED,
    /** A step failed and at least one compensation could not be completed: needs a human. */
    COMPENSATION_FAILED
}
