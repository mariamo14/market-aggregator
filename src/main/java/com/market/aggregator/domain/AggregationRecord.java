package com.market.aggregator.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//This class is used to store the aggregation of trades for a specific ticker.
@Data
public class AggregationRecord {
    private final String ticker;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highestPrice;
    private BigDecimal lowestPrice;
    private BigDecimal volumeOfTrades;

    private AggregationRecord(String ticker) {
        this.ticker = ticker;
    }

    public static AggregationRecord of(String stockName) {
        return new AggregationRecord(stockName);
    }

    public void recordTrade(Trade trade) {
        if (!trade.getTicker().equals(ticker)) {
            throw new IllegalArgumentException(String.format("Trying to aggregate ticker of type %s with type %s", trade.getTicker(), ticker));
        }

        if (openTime == null || trade.getTimestamp().isBefore(openTime)) {
            openTime = trade.getTimestamp();
            openPrice = trade.getPrice();
        }

        if (closeTime == null || trade.getTimestamp().isAfter(closeTime)) {
            closeTime = trade.getTimestamp();
            closePrice = trade.getPrice();
        }

        highestPrice = (highestPrice == null) ? trade.getPrice() : highestPrice.max(trade.getPrice());
        lowestPrice = (lowestPrice == null) ? trade.getPrice() : lowestPrice.min(trade.getPrice());

        if (volumeOfTrades == null) {
            volumeOfTrades = BigDecimal.ZERO;
        }
        BigDecimal tradeVolume = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        volumeOfTrades = volumeOfTrades.add(tradeVolume);
    }
}