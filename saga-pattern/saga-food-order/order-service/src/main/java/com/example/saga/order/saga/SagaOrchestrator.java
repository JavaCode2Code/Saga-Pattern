package com.example.saga.order.saga;

import com.example.saga.order.Order;

/**
 * Orchestration-based saga: runs the participants in order and, on the first failure,
 * compensates every step that may have taken effect, in reverse order.
 */
public interface SagaOrchestrator {
    /** Run the saga for a freshly saved PENDING order and return it in its final state. */
    Order run(Order order);

    /** Compensate every step (idempotent) and cancel the order. Used for interrupted sagas. */
    Order abandon(Order order, String reason);
}
