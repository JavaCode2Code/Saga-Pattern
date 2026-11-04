package com.example.saga.order;

import com.example.saga.order.saga.SagaOrchestrator;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * If the orchestrator dies mid-saga the order stays PENDING forever. This job finds such orders
 * and compensates every step; that is safe because compensations are idempotent no-ops for
 * steps that never ran.
 */
@Component
class StuckSagaRecovery {
    private static final Logger LOG = LoggerFactory.getLogger(StuckSagaRecovery.class);

    private final OrderRepository orders;
    private final SagaOrchestrator saga;
    private final long stuckAfterSeconds;

    StuckSagaRecovery(OrderRepository orders, SagaOrchestrator saga,
                      @Value("${saga.stuck-after-seconds:60}") long stuckAfterSeconds) {
        this.orders = orders;
        this.saga = saga;
        this.stuckAfterSeconds = stuckAfterSeconds;
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    void recover() {
        Instant cutoff = Instant.now().minus(Duration.ofSeconds(stuckAfterSeconds));
        for (Order o : orders.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff)) {
            LOG.warn("Order {} stuck in PENDING since {}, compensating", o.getId(), o.getCreatedAt());
            saga.abandon(o, "Saga did not finish within " + stuckAfterSeconds + "s");
        }
    }
}
