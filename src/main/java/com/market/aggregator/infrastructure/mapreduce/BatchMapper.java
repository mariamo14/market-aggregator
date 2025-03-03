package com.market.aggregator.infrastructure.mapreduce;

import com.market.aggregator.domain.Trade;
import com.market.aggregator.infrastructure.TradeParserUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BatchMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] lines = value.toString().split("\n");
        for (String line : lines) {
            if (isValidLine(line)) {
                Trade trade = parseTrade(line.trim());
                // Emit with date as key for grouping
                context.write(new Text(trade.getDate()), new Text(line));
            }
        }
    }

    private boolean isValidLine(String line) {
        return !line.isEmpty() && !line.startsWith("#") && !line.toLowerCase().startsWith("date");
    }

    private Trade parseTrade(String line) {
        String[] parts = line.split(";");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid line format: " + line);
        }
        return TradeParserUtil.getTrade(parts, DATE_TIME_FORMATTER);
    }
}