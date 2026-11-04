package com.example.saga.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Stock {
    @Id
    private String itemId;
    private int available;
    private BigDecimal unitPrice;

    protected Stock() {}
    public Stock(String itemId, int available, BigDecimal unitPrice) {
        this.itemId = itemId; this.available = available; this.unitPrice = unitPrice;
    }
    public String getItemId() { return itemId; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
