package com.market.aggregator.printer;

import com.market.aggregator.domain.AggregationRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// This class is used to print the aggregation results.
public class AggregationPrinter {

    public void printDayAggregations(LocalDate date, Set<String> allTickers, Map<String, AggregationRecord> dayAggregation) throws IOException {
        System.out.println("Date: " + date);
        // Sort tickers to print in order.
        var sortedTickers = allTickers.stream().sorted().toList();

        //Output the aggregation results for each ticker.
        for (String ticker : sortedTickers) {
            AggregationRecord record = dayAggregation.get(ticker);
            //Print record if available and the closeTime belongs to the same day.
            if (record != null && record.getCloseTime() != null && record.getCloseTime().toLocalDate().equals(date)) {
                System.out.printf("Ticker: %s, Open: %.1f, Close: %.1f, High: %.1f, Low: %.1f, Volume: %.2f%n", ticker, record.getOpenPrice().doubleValue(),
                        record.getClosePrice().doubleValue(),
                        record.getHighestPrice().doubleValue(),
                        record.getLowestPrice().doubleValue(),
                        record.getVolumeOfTrades().doubleValue());
            } else {
                System.out.printf("Ticker: %s, Open: N/A, Close: N/A, High: N/A, Low: N/A, Volume: 0.00%n", ticker);
            }
        }
    }

    public void printMarketIndex(LocalDate date, BigDecimal indexValue) {
        if (indexValue != null) {
            System.out.printf("Index for %s: %.1f%n", date, indexValue.doubleValue());
        } else {
            System.out.printf("Index for %s: N/A%n", date);
        }
        System.out.println("--------------------------------------------------");
    }
    //Future Implementation: We can export the results to a CSV file.
}