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

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(this::isValidLine)
                    .map(this::parseTrade)
                    .collect(Collectors.toList());
        }
    }

    private boolean isValidLine(String line) {
        return !line.isEmpty() && !line.startsWith("#") && !line.toLowerCase().startsWith("date");
    }

    private Trade parseTrade(String line) {
        String[] parts = line.split(";");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid line format: " + line);
        }
        LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim(), DATE_TIME_FORMATTER);
        String ticker = parts[1].trim();
        BigDecimal price = new BigDecimal(parts[2].trim());
        int quantity = Integer.parseInt(parts[3].trim());
        return Trade.builder()
                .timestamp(timestamp)
                .ticker(ticker)
                .price(price)
                .quantity(quantity)
                .build();
    }
}