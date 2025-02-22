package com.market.aggregator.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        // Enforce name of 3-4 captial letters
        return new AggregationRecord(stockName);
    }

    public void recordTrade(Trade trade) {
        // Throw an exception if the trade ticker doesn't match the record's ticker.
        if (!trade.getTicker().equals(ticker)) {
            throw new IllegalArgumentException(String.format("Trying to aggregate ticker of type %s with type %s", trade.getTicker(), ticker));
        }

        // Update open time and price if needed.
        if (openTime == null || trade.getTimestamp().isBefore(openTime)) {
            openTime = trade.getTimestamp();
            openPrice = trade.getPrice();
        }

        // Update close time and price if needed.
        if (closeTime == null || trade.getTimestamp().isAfter(closeTime)) {
            closeTime = trade.getTimestamp();
            closePrice = trade.getPrice();
        }

        // Initialize highestPrice and lowestPrice if null, otherwise update them.
        highestPrice = (highestPrice == null) ? trade.getPrice() : highestPrice.max(trade.getPrice());
        lowestPrice = (lowestPrice == null) ? trade.getPrice() : lowestPrice.min(trade.getPrice());

        // Ensure volumeOfTrades is initialized.
        if (volumeOfTrades == null) {
            volumeOfTrades = BigDecimal.ZERO;
        }
        BigDecimal tradeVolume = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        volumeOfTrades = volumeOfTrades.add(tradeVolume);
    }
}