package com.example.saga.order.saga;

import com.example.saga.order.Order;
import com.example.saga.order.OrderStatus;
import com.example.saga.order.SagaLogger;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * No database transaction is held across the remote calls: each state change is saved on its own,
 * so the orchestrator itself can crash at any point and the recovery job can finish the work.
 */
@Service
public class DefaultSagaOrchestrator implements SagaOrchestrator {
    private final List<SagaParticipant> participants;
    private final SagaLogger log;
    private final int compensationRetries;

    public DefaultSagaOrchestrator(List<SagaParticipant> participants, SagaLogger log,
                                   @Value("${saga.compensation-retries:3}") int compensationRetries) {
        this.participants = participants;   // injected in @Order, i.e. execution order
        this.log = log;
        this.compensationRetries = compensationRetries;
    }

    @Override
    public Order run(Order order) {
        List<SagaParticipant> attempted = new ArrayList<>();
        for (SagaParticipant p : participants) {
            attempted.add(p);
            try {
                p.execute(order);
                log.record(order, p.name(), "EXECUTE", true, "ok");
            } catch (RuntimeException e) {
                log.record(order, p.name(), "EXECUTE", false, e.getMessage());
                order.setFailureReason(p.name() + " failed: " + e.getMessage());
                // The failed step is compensated too: a timeout does not prove it had no effect.
                return compensate(order, attempted);
            }
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return log.save(order);
    }

    @Override
    public Order abandon(Order order, String reason) {
        order.setFailureReason(reason);
        return compensate(order, participants);
    }

    private Order compensate(Order order, List<SagaParticipant> steps) {
        boolean allUndone = true;
        for (int i = steps.size() - 1; i >= 0; i--) {
            allUndone &= compensateWithRetry(order, steps.get(i));
        }
        order.setStatus(allUndone ? OrderStatus.CANCELLED : OrderStatus.COMPENSATION_FAILED);
        return log.save(order);
    }

    private boolean compensateWithRetry(Order order, SagaParticipant p) {
        String last = null;
        for (int attempt = 1; attempt <= compensationRetries; attempt++) {
            try {
                p.compensate(order);
                log.record(order, p.name(), "COMPENSATE", true, "ok (attempt " + attempt + ")");
                return true;
            } catch (RuntimeException e) {
                last = e.getMessage();
                log.record(order, p.name(), "COMPENSATE", false, "attempt " + attempt + ": " + last);
                if (attempt < compensationRetries) sleep(200L * attempt);
            }
        }
        return false;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
