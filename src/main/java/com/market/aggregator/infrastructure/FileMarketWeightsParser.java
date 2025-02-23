package com.market.aggregator.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileMarketWeightsParser {

    public Map<String, BigDecimal> parseMarketWeights(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(this::isValidLine)
                    .map(this::parseWeight)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    private boolean isValidLine(String line) {
        return !line.isEmpty() && !line.startsWith("#");
    }

    private Map.Entry<String, BigDecimal> parseWeight(String line) {
        String[] parts = line.split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid line format: " + line);
        }
        String ticker = parts[0].trim();
        BigDecimal weight = new BigDecimal(parts[1].trim());
        return Map.entry(ticker, weight);
    }
}
