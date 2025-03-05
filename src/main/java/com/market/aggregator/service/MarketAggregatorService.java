package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.infrastructure.FileMarketWeightsParser;
import com.market.aggregator.infrastructure.FileTradeParser;
import com.market.aggregator.printer.AggregationPrinter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// FUTURE IMPROVEMENT:
// To enhance performance, we can consider reading and processing the data in batches
// Currently, this is not implemented.

//This class is responsible for processing trades and calculating market indices.
@Service
public class MarketAggregatorService {
    private final FileTradeParser tradeParser;
    private final FileMarketWeightsParser weightsParser;
    private final MarketIndexCalculator marketIndexCalculator;
    private final ITickerAggregatorManager tickerAggregatorManager;
    private final AggregationPrinter printer = new AggregationPrinter();

    // Thread pool for processing trades concurrently
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
    );

    //We instantiate the class with the necessary dependencies
    public MarketAggregatorService(FileTradeParser tradeParser,
                                   FileMarketWeightsParser weightsParser,
                                   MarketIndexCalculator marketIndexCalculator,
                                   TickerAggregatorManager tickerAggregatorManager) {
        this.tradeParser = tradeParser;
        this.tickerAggregatorManager = tickerAggregatorManager;
        this.weightsParser = weightsParser;
        this.marketIndexCalculator = marketIndexCalculator;
    }

    public Map<String, AggregationRecord> getAggregationFor(LocalDate date) {
        return tickerAggregatorManager.getAggregationFor(date);
    }

    // Processes trades and calculates market indices
    public void processTrades(InputStream tradesFile, InputStream weightsFile) throws IOException {
        // 1. Parse the trades from the provided input stream
        var trades = tradeParser.parseTrades(tradesFile);

        // 2. Parse the market weights from the provided input stream
        var marketWeights = weightsParser.parseMarketWeights(weightsFile);

        // 3. Process the trades concurrently using a thread pool
        List<CompletableFuture<Void>> futures = trades.stream()
                .map(trade -> CompletableFuture.runAsync(() -> tickerAggregatorManager.recordTrade(trade), executorService))
                .toList();

        // 4. Wait for all trade processing tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 5. Retrieve all dates for which trades have been recorded
        SortedSet<LocalDate> dates = new TreeSet<>(tickerAggregatorManager.getAggregationDates());

        // 6. Build a set of all tickers that have been traded
        Set<String> allTickers = buildAllTickers(dates);

        // 7. Initialize the last known weighted prices for each ticker
        Map<String, BigDecimal> lastWeightedPrices = initializeLastWeightedPrices(marketWeights);

        BigDecimal lastIndexValue = null;

        // 8. Process each day and calculate the market index
        for (LocalDate date : dates) {
            lastIndexValue = processDay(date, marketWeights, lastWeightedPrices, lastIndexValue, allTickers);
        }
    }

    // Returns the first aggregation date
    public Set<LocalDate> getAggregationDates() {
        return tickerAggregatorManager.getAggregationDates();
    }

    // Builds a set of all tickers that have been traded across all dates.
    private Set<String> buildAllTickers(SortedSet<LocalDate> dates) {
        return dates.stream()
                //Get the aggregation for each date
                .flatMap(date -> tickerAggregatorManager.getAggregationFor(date).keySet().stream())
                //Collect all tickers into a set
                .collect(Collectors.toSet());
    }

    // Initializes the last known weighted prices for each ticker
    private Map<String, BigDecimal> initializeLastWeightedPrices(Map<String, BigDecimal> marketWeights) {
        Map<String, BigDecimal> lastPrices = new HashMap<>();
        for (String ticker : marketWeights.keySet()) {
            lastPrices.put(ticker, null);
        }
        return lastPrices;
    }

    // Processes a single day of trades and calculates the market index
    private BigDecimal processDay(LocalDate date,
                                  Map<String, BigDecimal> marketWeights,
                                  Map<String, BigDecimal> lastWeightedPrices,
                                  BigDecimal lastIndexValue,
                                  Set<String> allTickers) throws IOException {
        // Get the aggregation for the current day
        var dayAggregation = tickerAggregatorManager.getAggregationFor(date);
        // Print the daily aggregations
        printer.printDayAggregations(date, allTickers, dayAggregation);

        // Initialize variables for index calculation
        boolean completeForIndex = true;
        Map<String, BigDecimal> weightedPricesForToday = new HashMap<>();

        // Update last weighted prices and prepare today's weighted prices
        for (String ticker : marketWeights.keySet()) {
            // Get the aggregation record for the current ticker
            AggregationRecord record = dayAggregation.get(ticker);

            // Update last weighted prices and prepare today's weighted prices
            if (record != null && record.getClosePrice() != null) {
                // Update the last known price for the ticker
                lastWeightedPrices.put(ticker, record.getClosePrice());
                weightedPricesForToday.put(ticker, record.getClosePrice());
            } else if (lastWeightedPrices.get(ticker) != null) {
                // Use the last known price if today's price is not available
                weightedPricesForToday.put(ticker, lastWeightedPrices.get(ticker));
            } else {
                // If no price is available for the ticker, mark as incomplete
                completeForIndex = false;
                weightedPricesForToday.put(ticker, null);
            }
        }

        // Calculate the market index
        BigDecimal indexValue;
        // If the index is complete, calculate the index value
        if (completeForIndex) {
            indexValue = marketIndexCalculator.calculate(weightedPricesForToday, marketWeights);
            lastIndexValue = indexValue;
        } else {
            // If the index is incomplete, use the last known index value
            indexValue = lastIndexValue;
        }
        // Print the market index for the day
        printer.printMarketIndex(date, indexValue);
        return lastIndexValue;
    }
}