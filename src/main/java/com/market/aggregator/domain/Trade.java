package com.market.aggregator.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Trade {
    private final LocalDateTime timestamp;
    private final String ticker;
    private final BigDecimal price;
    private final int quantity;

    public String getDate() {
        return timestamp.toLocalDate().toString();
    }
}
