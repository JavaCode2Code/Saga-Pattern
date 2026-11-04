package com.example.saga.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    public record ChargeRequest(@NotNull Long orderId, @NotBlank String customerId,
                                @NotNull @DecimalMin("0.01") BigDecimal amount) {}

    private final PaymentService service;
    private final AccountRepository accounts;
    private final PaymentRepository payments;

    public PaymentController(PaymentService service, AccountRepository accounts, PaymentRepository payments) {
        this.service = service; this.accounts = accounts; this.payments = payments;
    }

    @PostMapping("/charge")
    public Payment charge(@Valid @RequestBody ChargeRequest r) {
        return service.charge(r.orderId(), r.customerId(), r.amount());
    }

    @PostMapping("/refund/{orderId}")
    public void refund(@PathVariable Long orderId) { service.refund(orderId); }

    @GetMapping("/accounts")
    public List<Account> accounts() { return accounts.findAll(); }

    @GetMapping
    public List<Payment> payments() { return payments.findAll(); }
}
