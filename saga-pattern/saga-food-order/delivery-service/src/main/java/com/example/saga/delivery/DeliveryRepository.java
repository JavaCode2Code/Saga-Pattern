package com.example.saga.delivery;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
}
