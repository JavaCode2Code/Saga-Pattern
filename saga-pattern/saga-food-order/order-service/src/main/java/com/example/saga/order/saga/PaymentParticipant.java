package com.example.saga.order.saga;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@org.springframework.core.annotation.Order(2)
public class PaymentParticipant implements SagaParticipant {
    private final RemoteCalls remote;
    private final String baseUrl;

    public PaymentParticipant(RemoteCalls remote, @Value("${saga.payment-url}") String baseUrl) {
        this.remote = remote;
        this.baseUrl = baseUrl;
    }

    @Override public String name() { return "CHARGE_PAYMENT"; }

    @Override
    public void execute(com.example.saga.order.Order order) {
        remote.post(baseUrl + "/api/payments/charge",
                Map.of("orderId", order.getId(), "customerId", order.getCustomerId(), "amount", order.getAmount()));
    }

    @Override
    public void compensate(com.example.saga.order.Order order) {
        remote.post(baseUrl + "/api/payments/refund/" + order.getId(), null);
    }
}
