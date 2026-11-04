package com.example.saga.payment;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {
    private final AccountRepository accounts;
    private final PaymentRepository payments;

    public PaymentService(AccountRepository accounts, PaymentRepository payments) {
        this.accounts = accounts;
        this.payments = payments;
    }

    /** Idempotent per order. */
    @Transactional
    public Payment charge(Long orderId, String customerId, BigDecimal amount) {
        var existing = payments.findByOrderId(orderId);
        if (existing.isPresent()) return existing.get();

        Account a = accounts.lockById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Unknown customer " + customerId));
        if (a.getBalance().compareTo(amount) < 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient balance: " + a.getBalance() + " available, " + amount + " required");
        a.setBalance(a.getBalance().subtract(amount));
        return payments.save(new Payment(orderId, customerId, amount));
    }

    /** Compensation. Idempotent, and a no-op when nothing was charged. */
    @Transactional
    public void refund(Long orderId) {
        payments.findByOrderId(orderId).ifPresent(p -> {
            if (p.getStatus() == Payment.Status.CHARGED) {
                accounts.lockById(p.getCustomerId()).ifPresent(a -> a.setBalance(a.getBalance().add(p.getAmount())));
                p.setStatus(Payment.Status.REFUNDED);
            }
        });
    }
}
