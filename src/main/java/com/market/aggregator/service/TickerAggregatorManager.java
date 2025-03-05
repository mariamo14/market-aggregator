package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.domain.Trade;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Primary
public class TickerAggregatorManager implements ITickerAggregatorManager {

    // This map holds the aggregated trade data for each day.
    // The key is the date, and the value is another map that holds data for each ticker.
    private final Map<LocalDate, Map<String, AggregationRecord>> aggregationMap;

    private LocalDate firstAggregationDate;
    private LocalDate lastAggregationDate;

    public TickerAggregatorManager() {
        this.aggregationMap = new ConcurrentHashMap<>();
        this.firstAggregationDate = null;
        this.lastAggregationDate = null;
    }

    //Record the trade for a specific ticker on a specific date.
    @Override
    public void recordTrade(Trade trade) {
        // Convert the trade timestamp to a date and use it as the key.
        // For that date, get or create a map for all tickers.
        // For that ticker, get or create an AggregationRecord and then update it with the trade.
        aggregationMap.computeIfAbsent(trade.getTimestamp().toLocalDate(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(trade.getTicker(), AggregationRecord::of)
                .recordTrade(trade);
    }

    // Get the aggregated data for a specific day.
    @Override
    public Map<String, AggregationRecord> getAggregationFor(LocalDate date) {
        // Return the data for that date, or an empty map if no data exists.
        return Collections.unmodifiableMap(aggregationMap.getOrDefault(date, new ConcurrentHashMap<>()));
    }

    // Get the earliest date for which we have aggregated data.
    @Override
    public LocalDate getFirstAggregationDate() {
        if (firstAggregationDate != null) {
            // If we already calculated it, just return it.
            return firstAggregationDate;
        }
        // Otherwise, find the smallest date in our map and store it.
        return firstAggregationDate = calculateFirstAggregationDate();
    }

    // Get the latest date for which we have aggregated data.
    @Override
    public LocalDate getLastAggregationDate() {
        if (lastAggregationDate != null) {
            // Return the already calculated last date if available.
            return lastAggregationDate;
        }
        // Otherwise, find the largest date in our map and store it.
        return lastAggregationDate = calculateLastAggregationDate();
    }

    // Get a set of all the dates for which we have aggregation data.
    @Override
    public Set<LocalDate> getAggregationDates() {
        // Return all the dates in our aggregation map, but don’t allow changes.
        return Collections.unmodifiableSet(aggregationMap.keySet());
    }

    // Find the earliest date (the smallest date value) in our aggregation map.
    private LocalDate calculateFirstAggregationDate() {
        return aggregationMap.keySet().stream()
                .min(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("Aggregation window is empty"));
    }

    // Find the latest date (the largest date value) in our aggregation map.
    private LocalDate calculateLastAggregationDate() {
        return aggregationMap.keySet().stream()
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("Aggregation window is empty"));
    }
}