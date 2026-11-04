package com.example.saga.delivery;

import jakarta.persistence.*;

@Entity
public class Delivery {
    public enum Status { SCHEDULED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long orderId;
    private String address;
    @Enumerated(EnumType.STRING)
    private Status status = Status.SCHEDULED;

    protected Delivery() {}
    public Delivery(Long orderId, String address) { this.orderId = orderId; this.address = address; }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getAddress() { return address; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
