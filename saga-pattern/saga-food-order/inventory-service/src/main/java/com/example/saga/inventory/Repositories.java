package com.example.saga.inventory;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface StockRepository extends JpaRepository<Stock, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.itemId = :id")
    Optional<Stock> lockById(@Param("id") String id);
}

interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByOrderId(Long orderId);
}
