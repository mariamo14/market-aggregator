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
        // Start with an index value of zero.
        BigDecimal indexValue = BigDecimal.ZERO;

        // Loop over each ticker and its weight from the marketWeights map.
        for (Map.Entry<String, BigDecimal> entry : marketWeights.entrySet()) {
            String ticker = entry.getKey();
            BigDecimal weight = entry.getValue();

            // Get the weighted price for this ticker from the weightedPrices map.
            BigDecimal price = weightedPrices.get(ticker);

            // If the price is missing, stop and signal an error.
            if (price == null) {
                throw new IllegalArgumentException("Price for ticker " + ticker + " is null during index calculation");
            }
            // Similarly, if the weight is missing, signal an error.
            if (weight == null) {
                throw new IllegalArgumentException("Market weight for ticker " + ticker + " is null during index calculation");
            }

            // Multiply the price by the weight and add the result to the overall index value.
            indexValue = indexValue.add(price.multiply(weight));
        }
        return indexValue;
    }
}