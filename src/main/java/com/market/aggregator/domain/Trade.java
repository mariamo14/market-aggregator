package com.market.aggregator.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Trade {
    private final String date;
    private final String timestamp;
    private final String ticker;
    private final double price;
    private final int quantity;
}
