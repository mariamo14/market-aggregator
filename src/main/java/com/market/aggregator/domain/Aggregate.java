package com.market.aggregator.domain;

import lombok.Data;

@Data
public class Aggregate {
    private double open;
    private double close;
    private double high;
    private double low;
    private double volume;
    private boolean first = true;

    public void update(double price, int quantity) {
        if (first) {
            open = price;
            high = price;
            low = price;
            first = false;
        }
        close = price;
        if (price > high) {
            high = price;
        }
        if (price < low) {
            low = price;
        }
        volume += price * quantity;
    }
}
