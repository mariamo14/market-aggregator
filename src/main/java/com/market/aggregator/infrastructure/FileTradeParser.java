package com.market.aggregator.infrastructure;

import com.market.aggregator.domain.Trade;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileTradeParser {

    // Create a formatter that matches "2025-01-20 09:00:01"
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .map(String::trim)
                    // Skip empty lines, comments, and header line (which starts with "date+time")
                    .filter(line -> !line.isEmpty()
                            && !line.startsWith("#")
                            && !line.toLowerCase().startsWith("date"))
                    .map(line -> {
                        String[] parts = line.split(";");
                        if (parts.length < 4) {
                            throw new IllegalArgumentException("Invalid line format: " + line);
                        }
                        LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim(), DATE_TIME_FORMATTER);
                        String ticker = parts[1].trim();
                        BigDecimal price = new BigDecimal(parts[2].trim());
                        int quantity = Integer.parseInt(parts[3].trim());
                        // Use the builder to create a Trade instance
                        return Trade.builder()
                                .timestamp(timestamp)
                                .ticker(ticker)
                                .price(price)
                                .quantity(quantity)
                                .build();
                    })
                    .collect(Collectors.toList());
        }
    }
}