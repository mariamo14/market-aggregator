package com.market.aggregator.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AggregatorRecord {
    private final String date;
    // Mapping from ticker symbol to its aggregate data.
    private final Map<String, Aggregate> tickerAggregates;
    // Computed market index for the day.
    private final Double marketIndex;
}