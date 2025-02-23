package com.market.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

// This class is used to calculate the market index based on the weighted prices and market weights.
@Component
@Slf4j
public class MarketIndexCalculator {
    public BigDecimal calculate(Map<String, BigDecimal> weightedPrices, Map<String, BigDecimal> marketWeights) {
        BigDecimal indexValue = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : marketWeights.entrySet()) {
            String ticker = entry.getKey();
            BigDecimal weight = entry.getValue();
            BigDecimal price = weightedPrices.get(ticker);
            // Throw an exception if any price or weight is null.
            if (price == null) {
                throw new IllegalArgumentException("Price for ticker " + ticker + " is null during index calculation");
            }
            if (weight == null) {
                throw new IllegalArgumentException("Market weight for ticker " + ticker + " is null during index calculation");
            }
            indexValue = indexValue.add(price.multiply(weight));
        }
        return indexValue;
    }
}