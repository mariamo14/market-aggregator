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
        // Validate that the trade's ticker matches the record's ticker.
        if (!trade.getTicker().equals(ticker)) {
            throw new IllegalArgumentException(String.format("Trying to aggregate ticker of type %s with type %s", trade.getTicker(), ticker));
        }
        // Update the open time and open price if this is the first trade of the day.
        if (openTime == null || trade.getTimestamp().isBefore(openTime)) {
            openTime = trade.getTimestamp();
            openPrice = trade.getPrice();
        }

        // Update the close time and close price if this trade is the latest one.
        if (closeTime == null || trade.getTimestamp().isAfter(closeTime)) {
            closeTime = trade.getTimestamp();
            closePrice = trade.getPrice();
        }

        // Set the highest price seen so far (or update it if this trade's price is higher).
        highestPrice = (highestPrice == null) ? trade.getPrice() : highestPrice.max(trade.getPrice());
        // Set the lowest price seen so far (or update it if this trade's price is lower).
        lowestPrice = (lowestPrice == null) ? trade.getPrice() : lowestPrice.min(trade.getPrice());

        // Initialize volumeOfTrades if it's not set yet.
        if (volumeOfTrades == null) {
            volumeOfTrades = BigDecimal.ZERO;
        }
        // Calculate the trade's volume (price multiplied by quantity).
        BigDecimal tradeVolume = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        // Add the trade's volume to the total volume.
        volumeOfTrades = volumeOfTrades.add(tradeVolume);
    }
}