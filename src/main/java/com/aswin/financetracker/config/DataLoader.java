package com.aswin.financetracker.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    public DataLoader() {
    }

    @Override
    public void run(String... args) {
        log.info("Application initialized with clean state (no automatic dummy/sample financial data).");
    }
}

