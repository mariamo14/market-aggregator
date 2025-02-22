package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.domain.Trade;
import com.market.aggregator.infrastructure.FileMarketWeightsParser;
import com.market.aggregator.infrastructure.FileTradeParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarketAggregatorService {

    private final FileTradeParser tradeParser;
    private final FileMarketWeightsParser weightsParser;
    private final MarketIndexCalculator marketIndexCalculator;
    private final ITickerAggregatorManager tickerAggregatorManager;

    public MarketAggregatorService(FileTradeParser tradeParser,
                                   FileMarketWeightsParser weightsParser,
                                   MarketIndexCalculator marketIndexCalculator,
                                   TickerAggregatorManager tickerAggregatorManager) {
        this.tradeParser = tradeParser;
        this.tickerAggregatorManager = tickerAggregatorManager;
        this.weightsParser = weightsParser;
        this.marketIndexCalculator = marketIndexCalculator;
    }

    public void processTrades(InputStream tradesFile, InputStream weightsFile) throws IOException {
        // Parse the trades and weights from the files.
        List<Trade> trades = tradeParser.parseTrades(tradesFile);
        Map<String, BigDecimal> marketWeights = weightsParser.parseMarketWeights(weightsFile);

        // Record each trade.
        trades.forEach(tickerAggregatorManager::recordTrade);

        // This map will maintain the cumulative state across days.
        Map<String, AggregationRecord> currentState = new HashMap<>();
        boolean canConstructIndex = false;
        BigDecimal lastIndexValue = null;

        // Iterate through each day from the first to the last aggregation date.
        for (LocalDate date = tickerAggregatorManager.getFirstAggregationDate();
             !date.isAfter(tickerAggregatorManager.getLastAggregationDate());
             date = date.plusDays(1)) {

            // Get the aggregation records for the current day.
            Map<String, AggregationRecord> dayAggregation = tickerAggregatorManager.getAggregationFor(date);
            currentState.putAll(dayAggregation);

            // Determine if we now have all the market tickers needed to calculate the index.
            if (!canConstructIndex &&
                    marketWeights.keySet().stream().allMatch(currentState::containsKey)) {
                canConstructIndex = true;
            }

            System.out.println("Date: " + date);
            // Print daily trade aggregates for each ticker in the market weights.
            for (String ticker : marketWeights.keySet()) {
                AggregationRecord record = currentState.get(ticker);
                if (record != null && record.getCloseTime() != null
                        && record.getCloseTime().toLocalDate().isEqual(date)) {
                    System.out.printf("%s - Open: %s, Close: %s, High: %s, Low: %s, Volume: %s%n",
                            ticker,
                            record.getOpenPrice(), record.getClosePrice(),
                            record.getHighestPrice(), record.getLowestPrice(),
                            record.getVolumeOfTrades());
                } else {
                    // When there are no trades for the ticker on the day.
                    System.out.printf("%s - Open: N/A, Close: N/A, High: N/A, Low: N/A, Volume: 0%n", ticker);
                }
            }

            // Calculate and print the market index for the day.
            if (canConstructIndex) {
                // Calculate the index using the current state and market weights.
                // The index calculator should use the last known price for tickers with no trades today.
                BigDecimal indexValue = marketIndexCalculator.calculate(currentState, marketWeights, lastIndexValue, date);
                lastIndexValue = indexValue;
                System.out.printf("Index for %s: %s%n", date, indexValue);
            } else {
                // Not all weighted tickers have traded yet, so index is not available.
                System.out.printf("Index for %s: N/A%n", date);
            }
            System.out.println("--------------------------------------------------");
        }
    }

    /*private List<AggregatorRecord> aggregationRecord(Map<String, Double> marketWeights, List<Trade> trades) throws IOException {
        // AggregationRecord Tickers
        trades.forEach(tickerAggregatorManager::recordTrade);

        Map<String, AggregationRecord> currentState = new HashMap<>();
        boolean canConstructIndex = false;

        for (LocalDate date = tickerAggregatorManager.getFirstAggregationDate();
             !date.isAfter(tickerAggregatorManager.getLastAggregationDate());
             date = date.plusDays(1)) {

            Map<String, AggregationRecord> dayAggregation = tickerAggregatorManager.getAggregationFor(date);

            currentState.putAll(dayAggregation);

            if (!canConstructIndex
                    && marketWeights.keySet().stream().allMatch(currentState::containsKey)) {
                canConstructIndex = true;
            }
            // print the index;
            // print daily trade.
            for (Map.Entry<String, AggregationRecord> tickerAggregate : currentState.entrySet()) {
                if (tickerAggregate.getValue().getCloseTime().toLocalDate().isEqual(date)) {
                    // print;
                } else {
                    //print empty;
                }
            }

            if (canConstructIndex) {
                // Calcuate and print index
            }


        }
    }*/
}