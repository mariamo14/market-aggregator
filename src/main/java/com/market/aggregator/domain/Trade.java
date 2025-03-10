package com.market.aggregator.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//This class is used to store the trades of a specific ticker.
@Data
@Builder
public class Trade {
    // The fields of the Trade class match the parsed data from the historical log.
    private final LocalDateTime timestamp;
    private final String ticker;
    private final BigDecimal price;
    private final int quantity;

    public String getDate() {
        return timestamp.toLocalDate().toString();
    }
}
