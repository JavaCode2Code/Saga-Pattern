package com.example.saga.order.saga;

import com.example.saga.order.Order;

/**
 * One local transaction in the saga plus the action that semantically undoes it.
 * Both calls must be idempotent: the orchestrator may repeat them after timeouts or crashes.
 */
public interface SagaParticipant {
    String name();

    /** Run the step. Throw StepFailedException (or any RuntimeException) to fail the saga. */
    void execute(Order order);

    /** Undo the step. Must be safe when the step never ran, ran once, or already compensated. */
    void compensate(Order order);
}
