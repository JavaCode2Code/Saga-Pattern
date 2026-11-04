package com.example.saga.order.saga;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@org.springframework.core.annotation.Order(3)
public class DeliveryParticipant implements SagaParticipant {
    private final RemoteCalls remote;
    private final String baseUrl;

    public DeliveryParticipant(RemoteCalls remote, @Value("${saga.delivery-url}") String baseUrl) {
        this.remote = remote;
        this.baseUrl = baseUrl;
    }

    @Override public String name() { return "SCHEDULE_DELIVERY"; }

    @Override
    public void execute(com.example.saga.order.Order order) {
        remote.post(baseUrl + "/api/deliveries/schedule",
                Map.of("orderId", order.getId(), "address", order.getAddress()));
    }

    @Override
    public void compensate(com.example.saga.order.Order order) {
        remote.post(baseUrl + "/api/deliveries/cancel/" + order.getId(), null);
    }
}
