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

// This class is used to parse the trades from a file.
@Component
public class FileTradeParser {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().map(String::trim).filter(this::isValidLine).map(this::parseTrade).collect(Collectors.toList());
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
        return TradeParserUtil.getTrade(parts, DATE_TIME_FORMATTER);
    }
}