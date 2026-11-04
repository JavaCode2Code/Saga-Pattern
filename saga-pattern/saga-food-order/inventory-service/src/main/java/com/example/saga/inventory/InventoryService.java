package com.example.saga.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
    private final StockRepository stock;
    private final ReservationRepository reservations;

    public InventoryService(StockRepository stock, ReservationRepository reservations) {
        this.stock = stock;
        this.reservations = reservations;
    }

    /** Idempotent: calling twice for the same order returns the first reservation. */
    @Transactional
    public Reservation reserve(Long orderId, String itemId, int quantity) {
        var existing = reservations.findByOrderId(orderId);
        if (existing.isPresent()) return existing.get();

        Stock s = stock.lockById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Unknown item " + itemId));
        if (s.getAvailable() < quantity)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only " + s.getAvailable() + " of " + itemId + " left, requested " + quantity);
        s.setAvailable(s.getAvailable() - quantity);
        return reservations.save(new Reservation(orderId, itemId, quantity, s.getUnitPrice()));
    }

    /** Compensation. Idempotent, and a no-op when nothing was reserved. */
    @Transactional
    public void release(Long orderId) {
        reservations.findByOrderId(orderId).ifPresent(r -> {
            if (r.getStatus() == Reservation.Status.RESERVED) {
                stock.lockById(r.getItemId()).ifPresent(s -> s.setAvailable(s.getAvailable() + r.getQuantity()));
                r.setStatus(Reservation.Status.RELEASED);
            }
        });
    }
}
