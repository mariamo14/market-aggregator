package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.domain.Trade;
import com.market.aggregator.infrastructure.FileMarketWeightsParser;
import com.market.aggregator.infrastructure.FileTradeParser;
import com.market.aggregator.infrastructure.mapreduce.BatchJobRunner;
import com.market.aggregator.infrastructure.mapreduce.BatchLineInputFormat;
import com.market.aggregator.infrastructure.mapreduce.BatchMapper;
import com.market.aggregator.infrastructure.mapreduce.BatchReducer;
import com.market.aggregator.printer.AggregationPrinter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
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
// To enhance performance, we can consider reading and processing the data in batches instead of processing
// each trade individually. This would reduce the number of I/O calls and lower the overhead of submitting
// individual tasks. Currently, this is not implemented.

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
        var trades = tradeParser.parseTrades(tradesFile);
        var marketWeights = weightsParser.parseMarketWeights(weightsFile);

        // Process trades concurrently
        List<CompletableFuture<Void>> futures = trades.stream()
                .map(trade -> CompletableFuture.runAsync(() -> tickerAggregatorManager.recordTrade(trade), executorService))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Calculate market indices
        SortedSet<LocalDate> dates = new TreeSet<>(tickerAggregatorManager.getAggregationDates());
        Set<String> allTickers = buildAllTickers(dates);
        Map<String, BigDecimal> lastWeightedPrices = initializeLastWeightedPrices(marketWeights);

        // Process each day and calculate the market index
        BigDecimal lastIndexValue = null;
        for (LocalDate date : dates) {
            lastIndexValue = processDay(date, marketWeights, lastWeightedPrices, lastIndexValue, allTickers);
        }
    }

    // Processes trades in batches using MapReduce
    public void processTradesInBatches(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();
        conf.set("batch.record.lines", "5");
        Job job = Job.getInstance(conf, "Market Aggregator Batch Processing");
        job.setJarByClass(getClass());

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setMapperClass(BatchMapper.class);
        job.setReducerClass(BatchReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        if (job.waitForCompletion(true)) {
            processOutput(new Path(outputPath));
        } else {
            throw new IOException("MapReduce job failed");
        }
    }

    // Processes the output of the MapReduce job
    private void processOutput(Path outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(outputPath.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processDailyAggregation(
                        LocalDate.parse(line.substring(0, line.indexOf(";"))),
                        line.substring(line.indexOf(";") + 1)
                );
            }
        }
    }

    private void processDailyAggregation(LocalDate date, String aggregation) {
        String[] parts = aggregation.split(";");
        if (parts.length == 6) {
            AggregationRecord record = AggregationRecord.of(parts[0]);
            record.setOpenPrice(new BigDecimal(parts[1]));
            record.setClosePrice(new BigDecimal(parts[2]));
            record.setHighestPrice(new BigDecimal(parts[3]));
            record.setLowestPrice(new BigDecimal(parts[4]));
            record.setVolumeOfTrades(new BigDecimal(parts[5]));
            record.setOpenTime(date.atStartOfDay());
            record.setCloseTime(date.atTime(23, 59, 59));
            tickerAggregatorManager.recordTrade(buildTradeFromRecord(date, record));
        }
    }

    private Trade buildTradeFromRecord(LocalDate date, AggregationRecord record) {
        return Trade.builder()
                .ticker(record.getTicker())
                .price(record.getClosePrice())
                .quantity(record.getVolumeOfTrades().intValue())
                .timestamp(date.atTime(12, 0))
                .build();
    }

    public Set<LocalDate> getAggregationDates() {
        return tickerAggregatorManager.getAggregationDates();
    }

    private Set<String> buildAllTickers(SortedSet<LocalDate> dates) {
        return dates.stream()
                .flatMap(date -> tickerAggregatorManager.getAggregationFor(date).keySet().stream())
                .collect(Collectors.toSet());
    }

    // Initializes the last weighted prices for each ticker
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
                                  Set<String> allTickers) {
        var dayAggregation = tickerAggregatorManager.getAggregationFor(date);
        printer.printDayAggregations(date, allTickers, dayAggregation);

        boolean completeForIndex = true;
        Map<String, BigDecimal> weightedPricesForToday = new HashMap<>();

        // Update last weighted prices and prepare today's weighted prices
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

        // Calculate the market index
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