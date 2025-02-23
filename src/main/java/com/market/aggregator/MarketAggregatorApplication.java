package com.market.aggregator;

import com.market.aggregator.service.MarketAggregatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@Slf4j
public class MarketAggregatorApplication implements CommandLineRunner {

    private final MarketAggregatorService aggregatorService;

    public MarketAggregatorApplication(MarketAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MarketAggregatorApplication.class, args);
    }

    // Helper method to get resource as InputStream
    private InputStream getResourceAsStream(String resourceName) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourceName);
        if (!resource.exists()) {
            throw new IllegalArgumentException("File not found: " + resourceName);
        }
        return resource.getInputStream();
    }

    // Main application logic
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting MarketAggregatorApplication...");

        InputStream logStream;
        if (args.length >= 1 && Files.exists(Path.of(args[0]))) {
            log.info("Using external file: {}", args[0]);
            logStream = new FileInputStream(args[0]);
        } else {
            log.info("No valid external file provided. Using default file from classpath.");
            logStream = getResourceAsStream("market_log.txt");
        }

        try (InputStream ls = logStream;
             InputStream weightsStream = getResourceAsStream("market_weights.txt")) {
            log.info("Processing trades...");
            aggregatorService.processTrades(ls, weightsStream);
            log.info("Processing completed.");
        }
    }
}