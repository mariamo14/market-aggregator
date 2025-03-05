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

    //The aggregator service is used to process the trades and calculate the index.
    //The application is a thin layer that delegates the work to the service.
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
        //We use stream to avoid loading the entire file into memory
        return resource.getInputStream();
    }

    // Main application logic
    @Override
    public void run(String... args) throws Exception {
        // Log the start of the application
        log.info("Starting MarketAggregatorApplication...");

        InputStream logStream;
        if (args.length >= 1 && Files.exists(Path.of(args[0]))) {
            // Use the external file if provided and valid
            log.info("Using external file: {}", args[0]);
            logStream = new FileInputStream(args[0]);
        } else {
            // Fallback to the default file in the classpath
            log.info("No valid external file provided. Using default file from classpath.");
            logStream = getResourceAsStream("market_log.txt");
        }

        // Process the trades
        try (InputStream ls = logStream;
             InputStream weightsStream = getResourceAsStream("market_weights.txt")) {
            log.info("Processing trades...");
            //Aggregator Service processes the trades
            aggregatorService.processTrades(ls, weightsStream);
            log.info("Processing completed.");
        }
        catch (Exception e) {
            //The trades could not be processed
            log.error("Error processing trades: {}", e.getMessage());
        }
    }
}