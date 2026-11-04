package com.example.saga.order.saga;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@org.springframework.core.annotation.Order(1)
public class InventoryParticipant implements SagaParticipant {
    private final RemoteCalls remote;
    private final String baseUrl;

    public InventoryParticipant(RemoteCalls remote, @Value("${saga.inventory-url}") String baseUrl) {
        this.remote = remote;
        this.baseUrl = baseUrl;
    }

    @Override public String name() { return "RESERVE_INVENTORY"; }

    @Override
    public void execute(com.example.saga.order.Order order) {
        JsonNode r = remote.post(baseUrl + "/api/inventory/reserve",
                Map.of("orderId", order.getId(), "itemId", order.getItemId(), "quantity", order.getQuantity()));
        // The price comes from inventory, never from the client.
        BigDecimal unit = new BigDecimal(r.path("unitPrice").asText());
        order.setUnitPrice(unit);
        order.setAmount(unit.multiply(BigDecimal.valueOf(order.getQuantity())));
    }

    @Override
    public void compensate(com.example.saga.order.Order order) {
        remote.post(baseUrl + "/api/inventory/release/" + order.getId(), null);
    }
}
