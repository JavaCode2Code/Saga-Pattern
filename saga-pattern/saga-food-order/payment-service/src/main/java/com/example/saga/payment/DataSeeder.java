package com.example.saga.payment;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class DataSeeder implements CommandLineRunner {
    private final AccountRepository accounts;
    DataSeeder(AccountRepository accounts) { this.accounts = accounts; }

    @Override
    public void run(String... args) {
        accounts.save(new Account("alice", new BigDecimal("1000.00")));
        accounts.save(new Account("bob", new BigDecimal("50.00")));
    }
}
