package com.example.saga.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    public record ScheduleRequest(@NotNull Long orderId, @NotBlank String address) {}

    private final DeliveryRepository repo;

    public DeliveryController(DeliveryRepository repo) { this.repo = repo; }

    /** Idempotent per order. Addresses containing "UNSERVICEABLE" are rejected, to demo a failing last step. */
    @PostMapping("/schedule")
    @Transactional
    public Delivery schedule(@Valid @RequestBody ScheduleRequest r) {
        var existing = repo.findByOrderId(r.orderId());
        if (existing.isPresent()) return existing.get();
        if (r.address().toUpperCase().contains("UNSERVICEABLE"))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No delivery partner serves this address");
        return repo.save(new Delivery(r.orderId(), r.address()));
    }

    /** Compensation. Idempotent, and a no-op when nothing was scheduled. */
    @PostMapping("/cancel/{orderId}")
    @Transactional
    public void cancel(@PathVariable Long orderId) {
        repo.findByOrderId(orderId).ifPresent(d -> d.setStatus(Delivery.Status.CANCELLED));
    }

    @GetMapping
    public List<Delivery> all() { return repo.findAll(); }
}
