package com.market.aggregator.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AggregateTest {

    @Test
    void testUpdate() {
        Aggregate aggregate = new Aggregate();

        aggregate.update(10.0d, 1);

        assertEquals(10.0d, aggregate.getClose());
        assertEquals(10.0d, aggregate.getHigh());
        assertEquals(10.0d, aggregate.getLow());
        assertEquals(10.0d, aggregate.getOpen());
        assertEquals(10.0d, aggregate.getVolume());
        assertFalse(aggregate.isFirst());
    }

    @Test
    void testUpdate2() {
        Aggregate aggregate = new Aggregate();
        aggregate.setFirst(false);

        aggregate.update(10.0d, 1);

        assertEquals(10.0d, aggregate.getClose());
        assertEquals(10.0d, aggregate.getHigh());
        assertEquals(10.0d, aggregate.getVolume());
    }

    @Test
    void testUpdate3() {
        Aggregate aggregate = new Aggregate();
        aggregate.setFirst(false);

        aggregate.update(-0.5d, 1);

        assertEquals(-0.5d, aggregate.getClose());
        assertEquals(-0.5d, aggregate.getLow());
        assertEquals(-0.5d, aggregate.getVolume());
    }

    @Test
    void testUpdateCalculations() {
        Aggregate aggregate = new Aggregate();
        // First trade sets open, high, low, and close.
        aggregate.update(100.0, 500);
        // Second trade updates close, high, volume.
        aggregate.update(105.0, 600);

        // Verify open, close, high, low values.
        assertEquals(100.0, aggregate.getOpen(), 0.001);
        assertEquals(105.0, aggregate.getClose(), 0.001);
        assertEquals(105.0, aggregate.getHigh(), 0.001);
        assertEquals(100.0, aggregate.getLow(), 0.001);

        // Verify volume is computed as price * quantity.
        double expectedVolume = 100.0 * 500 + 105.0 * 600;
        assertEquals(expectedVolume, aggregate.getVolume(), 0.001);
    }
}
