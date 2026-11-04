package com.example.saga.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Account {
    @Id
    private String customerId;
    private BigDecimal balance;

    protected Account() {}
    public Account(String customerId, BigDecimal balance) { this.customerId = customerId; this.balance = balance; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
