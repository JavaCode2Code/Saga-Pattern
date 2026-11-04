package com.example.saga.order;

import jakarta.persistence.*;
import java.time.Instant;

/** One row per attempted step or compensation: the saga's audit trail. */
@Entity
public class SagaLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String step;
    private String action;
    private boolean success;
    @Column(length = 500)
    private String message;
    private Instant occurredAt = Instant.now();

    protected SagaLog() {}
    public SagaLog(Long orderId, String step, String action, boolean success, String message) {
        this.orderId = orderId; this.step = step; this.action = action; this.success = success;
        this.message = message != null && message.length() > 500 ? message.substring(0, 500) : message;
    }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getStep() { return step; }
    public String getAction() { return action; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Instant getOccurredAt() { return occurredAt; }
}
