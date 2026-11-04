package com.example.saga.inventory;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class DataSeeder implements CommandLineRunner {
    private final StockRepository stock;
    DataSeeder(StockRepository stock) { this.stock = stock; }

    @Override
    public void run(String... args) {
        stock.save(new Stock("PIZZA", 10, new BigDecimal("300.00")));
        stock.save(new Stock("BURGER", 2, new BigDecimal("150.00")));
    }
}
