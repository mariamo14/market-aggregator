package com.market.aggregator;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.service.MarketAggregatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MarketAggregatorIntegrationTests {

    @Autowired
    private MarketAggregatorService aggregatorService;

    @Test
    public void integrationTestProcessTradesAndAggregations() throws Exception {
        // Load test files from the classpath
        ClassPathResource logResource = new ClassPathResource("market_log.txt");
        ClassPathResource weightResource = new ClassPathResource("market_weights.txt");

        try (InputStream logStream = logResource.getInputStream();
             InputStream weightStream = weightResource.getInputStream()) {
            aggregatorService.processTrades(logStream, weightStream);
        }

        // Validate aggregation for a known date (adjust the expected date if needed)
        LocalDate targetDate = LocalDate.of(2025, 1, 20);
        Map<String, AggregationRecord> aggregations = aggregatorService.getAggregationFor(targetDate);
        assertFalse(aggregations.isEmpty(), "Aggregations should not be empty for date " + targetDate);

        AggregationRecord recordABC = aggregations.get("ABC");
        assertNotNull(recordABC, "Expected aggregation record for ticker ABC");
        assertNotNull(recordABC.getClosePrice(), "Expected close price for ticker ABC");
    }

    @Test
    public void integrationTestMarketIndexCalculation() throws Exception {
        // Load test files from the classpath
        ClassPathResource logResource = new ClassPathResource("market_log.txt");
        ClassPathResource weightResource = new ClassPathResource("market_weights.txt");

        try (InputStream logStream = logResource.getInputStream();
             InputStream weightStream = weightResource.getInputStream()) {
            aggregatorService.processTrades(logStream, weightStream);
        }

        // Validate that aggregations are available for a given date
        LocalDate targetDate = LocalDate.of(2025, 1, 20);
        Map<String, AggregationRecord> aggregations = aggregatorService.getAggregationFor(targetDate);
        assertFalse(aggregations.isEmpty(), "Aggregations should not be empty for date " + targetDate);

        // Check that a weighted ticker (e.g., ABC) has been processed and has a valid close price
        AggregationRecord recordABC = aggregations.get("ABC");
        assertNotNull(recordABC, "Aggregation record for ticker ABC should exist");
        assertNotNull(recordABC.getClosePrice(), "Close price for ticker ABC should be present");
    }
}