package com.market.aggregator.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class FileMarketWeightsParser {

    public Map<String, BigDecimal> parseMarketWeights(InputStream inputStream) throws IOException {
        Map<String, BigDecimal> marketWeights = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        // Use colon as delimiter
                        String[] parts = line.split(":");
                        if (parts.length < 2) {
                            throw new IllegalArgumentException("Invalid line format: " + line);
                        }
                        String ticker = parts[0].trim();
                        BigDecimal weight = new BigDecimal(parts[1].trim());
                        marketWeights.put(ticker, weight);
                    });
        }

        return marketWeights;
    }
}