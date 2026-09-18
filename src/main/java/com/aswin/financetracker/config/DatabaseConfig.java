package com.aswin.financetracker.config;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    static {
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                log.info("Successfully created persistent database directory: {}", dataDir.getAbsolutePath());
            } else {
                log.warn("Could not create database directory: {}", dataDir.getAbsolutePath());
            }
        }
    }
}
