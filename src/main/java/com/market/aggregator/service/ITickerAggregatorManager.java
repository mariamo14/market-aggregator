package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.domain.Trade;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

// This interface is used to manage the aggregation of trades for each ticker.
public interface ITickerAggregatorManager {
    void recordTrade(Trade trade);

    Map<String, AggregationRecord> getAggregationFor(LocalDate date);

    LocalDate getFirstAggregationDate();

    LocalDate getLastAggregationDate();

    Set<LocalDate> getAggregationDates();
}