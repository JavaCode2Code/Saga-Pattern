package com.example.saga.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerId;
    private String itemId;
    private int quantity;
    private String address;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    private String failureReason;
    private Instant createdAt = Instant.now();

    protected Order() {}
    public Order(String customerId, String itemId, int quantity, String address) {
        this.customerId = customerId; this.itemId = itemId; this.quantity = quantity; this.address = address;
    }

    public Long getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public String getAddress() { return address; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
}
