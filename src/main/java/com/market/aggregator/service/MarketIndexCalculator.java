package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
@Slf4j
public class MarketIndexCalculator {
    public BigDecimal calculate(Map<String, AggregationRecord> currentState,
                                Map<String, BigDecimal> marketWeights,
                                BigDecimal lastIndexValue,
                                LocalDate date) {
        BigDecimal indexValue = BigDecimal.ZERO;
        boolean hasNewTrade = false;

        for (Map.Entry<String, BigDecimal> weightEntry : marketWeights.entrySet()) {
            String ticker = weightEntry.getKey();
            BigDecimal weight = weightEntry.getValue();
            AggregationRecord record = currentState.get(ticker);
            if (record != null && record.getCloseTime() != null && record.getClosePrice() != null) {
                LocalDate tradeDate = record.getCloseTime().toLocalDate();
                BigDecimal price = record.getClosePrice();
                // Mark that a new trade occurred today for this ticker.
                if (tradeDate.isEqual(date)) {
                    hasNewTrade = true;
                }
                indexValue = indexValue.add(price.multiply(weight));
            }
        }
        // If no new trade occurred for any weighted ticker on this day, return the last known index value.
        if (!hasNewTrade && lastIndexValue != null) {
            return lastIndexValue;
        }
        return indexValue;
    }
}