package com.aswin.financetracker.config;

import com.aswin.financetracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final UserService userService;

    public DataLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        // Ensure the default system user is created for single-user mode (required for foreign keys)
        userService.getDefaultUser();
        log.info("Application initialized with clean state (no automatic dummy/sample financial data).");
    }
}

