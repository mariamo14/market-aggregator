package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.domain.Trade;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface ITickerAggregatorManager {
    void recordTrade(Trade trade);

    Map<String, AggregationRecord> getAggregationFor(LocalDate date);

    LocalDate getFirstAggregationDate();

    LocalDate getLastAggregationDate();

    Set<LocalDate> getAggregationDates();
}