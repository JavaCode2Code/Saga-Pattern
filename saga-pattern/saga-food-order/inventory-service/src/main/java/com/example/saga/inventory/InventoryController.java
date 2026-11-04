package com.example.saga.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    public record ReserveRequest(@NotNull Long orderId, @NotBlank String itemId, @Min(1) int quantity) {}

    private final InventoryService service;
    private final StockRepository stock;
    private final ReservationRepository reservations;

    public InventoryController(InventoryService service, StockRepository stock, ReservationRepository reservations) {
        this.service = service; this.stock = stock; this.reservations = reservations;
    }

    @PostMapping("/reserve")
    public Reservation reserve(@Valid @RequestBody ReserveRequest r) {
        return service.reserve(r.orderId(), r.itemId(), r.quantity());
    }

    @PostMapping("/release/{orderId}")
    public void release(@PathVariable Long orderId) { service.release(orderId); }

    @GetMapping("/stock")
    public List<Stock> stock() { return stock.findAll(); }

    @GetMapping("/reservations")
    public List<Reservation> reservations() { return reservations.findAll(); }
}
