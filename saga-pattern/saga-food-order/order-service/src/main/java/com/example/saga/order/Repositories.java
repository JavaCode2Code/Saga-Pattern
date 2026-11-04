package com.example.saga.order;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant before);
}

interface SagaLogRepository extends JpaRepository<SagaLog, Long> {
    List<SagaLog> findByOrderIdOrderByIdAsc(Long orderId);
}
