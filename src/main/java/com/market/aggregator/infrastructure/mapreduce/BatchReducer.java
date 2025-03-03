package com.market.aggregator.infrastructure.mapreduce;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.domain.Trade;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BatchReducer extends Reducer<Text, Text, Text, Text> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        Map<String, AggregationRecord> aggregations = new HashMap<>();

        for (Text value : values) {
            Trade trade = parseTrade(value.toString());
            aggregations.computeIfAbsent(trade.getTicker(), AggregationRecord::of)
                    .recordTrade(trade);
        }

        // Output aggregations for each ticker
        for (Map.Entry<String, AggregationRecord> entry : aggregations.entrySet()) {
            AggregationRecord record = entry.getValue();
            String output = String.format("%s;%.2f;%.2f;%.2f;%.2f;%.2f",
                    entry.getKey(),
                    record.getOpenPrice(),
                    record.getClosePrice(),
                    record.getHighestPrice(),
                    record.getLowestPrice(),
                    record.getVolumeOfTrades());
            context.write(key, new Text(output));
        }
    }

    private Trade parseTrade(String line) {
        String[] parts = line.split(";");
        return getTrade(parts, DATE_TIME_FORMATTER);
    }

    public static Trade getTrade(String[] parts, DateTimeFormatter dateTimeFormatter) {
        LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim(), dateTimeFormatter);
        String ticker = parts[1].trim();
        BigDecimal price = new BigDecimal(parts[2].trim());
        int quantity = Integer.parseInt(parts[3].trim());
        return Trade.builder()
                .timestamp(timestamp)
                .ticker(ticker)
                .price(price)
                .quantity(quantity)
                .build();
    }
}