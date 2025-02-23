package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.infrastructure.FileMarketWeightsParser;
import com.market.aggregator.infrastructure.FileTradeParser;
import com.market.aggregator.printer.AggregationPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketAggregatorService {

    private final FileTradeParser tradeParser;
    private final FileMarketWeightsParser weightsParser;
    private final MarketIndexCalculator marketIndexCalculator;
    private final ITickerAggregatorManager tickerAggregatorManager;
    private final AggregationPrinter printer = new AggregationPrinter();

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

    public void processTrades(InputStream tradesFile, InputStream weightsFile) throws IOException {
        var trades = tradeParser.parseTrades(tradesFile);
        var marketWeights = weightsParser.parseMarketWeights(weightsFile);

        trades.forEach(tickerAggregatorManager::recordTrade);

        SortedSet<LocalDate> dates = new TreeSet<>(tickerAggregatorManager.getAggregationDates());
        Set<String> allTickers = buildAllTickers(dates);
        Map<String, BigDecimal> lastWeightedPrices = initializeLastWeightedPrices(marketWeights);

        BigDecimal lastIndexValue = null;
        for (LocalDate date : dates) {
            lastIndexValue = processDay(date, marketWeights, lastWeightedPrices, lastIndexValue, allTickers);
        }
    }

    public Set<LocalDate> getAggregationDates() {
        return tickerAggregatorManager.getAggregationDates();
    }

    private Set<String> buildAllTickers(SortedSet<LocalDate> dates) {
        return dates.stream()
                .flatMap(date -> tickerAggregatorManager.getAggregationFor(date).keySet().stream())
                .collect(Collectors.toSet());
    }

    private Map<String, BigDecimal> initializeLastWeightedPrices(Map<String, BigDecimal> marketWeights) {
        Map<String, BigDecimal> lastPrices = new HashMap<>();
        for (String ticker : marketWeights.keySet()) {
            lastPrices.put(ticker, null);
        }
        return lastPrices;
    }

    private BigDecimal processDay(LocalDate date,
                                  Map<String, BigDecimal> marketWeights,
                                  Map<String, BigDecimal> lastWeightedPrices,
                                  BigDecimal lastIndexValue,
                                  Set<String> allTickers) {
        var dayAggregation = tickerAggregatorManager.getAggregationFor(date);
        printer.printDayAggregations(date, allTickers, dayAggregation);

        boolean completeForIndex = true;
        Map<String, BigDecimal> weightedPricesForToday = new HashMap<>();
        for (String ticker : marketWeights.keySet()) {
            AggregationRecord record = dayAggregation.get(ticker);
            if (record != null && record.getClosePrice() != null) {
                lastWeightedPrices.put(ticker, record.getClosePrice());
                weightedPricesForToday.put(ticker, record.getClosePrice());
            } else if (lastWeightedPrices.get(ticker) != null) {
                weightedPricesForToday.put(ticker, lastWeightedPrices.get(ticker));
            } else {
                completeForIndex = false;
                weightedPricesForToday.put(ticker, null);
            }
        }

        BigDecimal indexValue;
        if (completeForIndex) {
            indexValue = marketIndexCalculator.calculate(weightedPricesForToday, marketWeights);
            lastIndexValue = indexValue;
        } else {
            indexValue = lastIndexValue;
        }
        printer.printMarketIndex(date, indexValue);
        return lastIndexValue;
    }
}
