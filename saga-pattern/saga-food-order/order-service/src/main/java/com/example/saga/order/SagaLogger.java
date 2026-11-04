package com.example.saga.order;

import java.util.List;
import org.springframework.stereotype.Component;

/** Persists saga audit rows and order state; each call is its own short transaction. */
@Component
public class SagaLogger {
    private final SagaLogRepository logs;
    private final OrderRepository orders;

    SagaLogger(SagaLogRepository logs, OrderRepository orders) {
        this.logs = logs;
        this.orders = orders;
    }

    public void record(Order order, String step, String action, boolean success, String message) {
        logs.save(new SagaLog(order.getId(), step, action, success, message));
    }

    public Order save(Order order) { return orders.save(order); }

    public List<SagaLog> history(Long orderId) { return logs.findByOrderIdOrderByIdAsc(orderId); }
}
