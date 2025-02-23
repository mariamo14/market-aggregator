package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.domain.Trade;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Primary
public class TickerAggregatorManager implements ITickerAggregatorManager {

    private final Map<LocalDate, Map<String, AggregationRecord>> aggregationMap;
    private LocalDate firstAggregationDate;
    private LocalDate lastAggregationDate;

    public TickerAggregatorManager() {
        this.aggregationMap = new HashMap<>();
        this.firstAggregationDate = null;
        this.lastAggregationDate = null;
    }

    @Override
    public void recordTrade(Trade trade) {
        aggregationMap.computeIfAbsent(trade.getTimestamp().toLocalDate(), k -> new HashMap<>())
                .computeIfAbsent(trade.getTicker(), AggregationRecord::of)
                .recordTrade(trade);
    }

    @Override
    public Map<String, AggregationRecord> getAggregationFor(LocalDate date) {
        return Collections.unmodifiableMap(aggregationMap.getOrDefault(date, new HashMap<>()));
    }

    @Override
    public LocalDate getFirstAggregationDate() {
        if (firstAggregationDate != null) {
            return firstAggregationDate;
        }
        return firstAggregationDate = calculateFirstAggregationDate();
    }

    @Override
    public LocalDate getLastAggregationDate() {
        if (lastAggregationDate != null) {
            return lastAggregationDate;
        }
        return lastAggregationDate = calculateLastAggregationDate();
    }

    @Override
    public Set<LocalDate> getAggregationDates() {
        return Collections.unmodifiableSet(aggregationMap.keySet());
    }

    private LocalDate calculateFirstAggregationDate() {
        return aggregationMap.keySet().stream()
                .min(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("Aggregation window is empty"));
    }

    private LocalDate calculateLastAggregationDate() {
        return aggregationMap.keySet().stream()
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("Aggregation window is empty"));
    }
}