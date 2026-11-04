package com.example.saga.order;

import com.example.saga.order.saga.SagaOrchestrator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    public record PlaceOrderRequest(@NotBlank String customerId, @NotBlank String itemId,
                                    @Min(1) int quantity, @NotBlank String address) {}

    private final OrderRepository orders;
    private final SagaOrchestrator saga;
    private final SagaLogger logger;

    public OrderController(OrderRepository orders, SagaOrchestrator saga, SagaLogger logger) {
        this.orders = orders;
        this.saga = saga;
        this.logger = logger;
    }

    /** Runs the saga synchronously. 201 with the final order; check "status" for CONFIRMED vs CANCELLED. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(@Valid @RequestBody PlaceOrderRequest r) {
        Order order = orders.save(new Order(r.customerId(), r.itemId(), r.quantity(), r.address()));
        return saga.run(order);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }

    @GetMapping
    public List<Order> all() { return orders.findAll(); }

    /** The saga's audit trail: every step and compensation, in order. */
    @GetMapping("/{id}/saga")
    public List<SagaLog> history(@PathVariable Long id) {
        get(id);
        return logger.history(id);
    }

    /** Retry compensation for an order stuck in COMPENSATION_FAILED. */
    @PostMapping("/{id}/retry-compensation")
    public Order retry(@PathVariable Long id) {
        Order o = get(id);
        if (o.getStatus() != OrderStatus.COMPENSATION_FAILED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is " + o.getStatus() + ", nothing to retry");
        return saga.abandon(o, o.getFailureReason());
    }
}
