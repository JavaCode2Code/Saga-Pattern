package com.example.saga.inventory;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Reservation {
    public enum Status { RESERVED, RELEASED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long orderId;
    private String itemId;
    private int quantity;
    private BigDecimal unitPrice;
    @Enumerated(EnumType.STRING)
    private Status status = Status.RESERVED;

    protected Reservation() {}
    public Reservation(Long orderId, String itemId, int quantity, BigDecimal unitPrice) {
        this.orderId = orderId; this.itemId = itemId; this.quantity = quantity; this.unitPrice = unitPrice;
    }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
