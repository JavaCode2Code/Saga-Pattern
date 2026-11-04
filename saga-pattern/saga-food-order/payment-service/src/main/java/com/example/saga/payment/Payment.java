package com.example.saga.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Payment {
    public enum Status { CHARGED, REFUNDED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long orderId;
    private String customerId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Status status = Status.CHARGED;

    protected Payment() {}
    public Payment(Long orderId, String customerId, BigDecimal amount) {
        this.orderId = orderId; this.customerId = customerId; this.amount = amount;
    }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
