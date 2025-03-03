package com.market.aggregator.infrastructure;

import com.market.aggregator.domain.Trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TradeParserUtil {
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