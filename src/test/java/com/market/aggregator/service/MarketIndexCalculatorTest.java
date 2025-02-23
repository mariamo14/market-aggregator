package com.market.aggregator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {MarketIndexCalculator.class})
@ExtendWith(SpringExtension.class)
class MarketIndexCalculatorTest {
    @Autowired
    private MarketIndexCalculator marketIndexCalculator;

    @Test
    void testCalculate() {
        HashMap<String, BigDecimal> weightedPrices = new HashMap<>();

        BigDecimal actualCalculateResult = marketIndexCalculator.calculate(weightedPrices, new HashMap<>());

        assertEquals(new BigDecimal("0"), actualCalculateResult);
        assertSame(BigDecimal.ZERO, actualCalculateResult);
    }

    @Test
    void testCalculate2() {
        HashMap<String, BigDecimal> weightedPrices = new HashMap<>();

        HashMap<String, BigDecimal> marketWeights = new HashMap<>();
        marketWeights.put("foo", new BigDecimal("2.3"));

        assertThrows(IllegalArgumentException.class, () -> marketIndexCalculator.calculate(weightedPrices, marketWeights));
    }


    @Test
    void testCalculate3() {
        HashMap<String, BigDecimal> weightedPrices = new HashMap<>();
        weightedPrices.put("foo", new BigDecimal("2.3"));

        HashMap<String, BigDecimal> marketWeights = new HashMap<>();
        marketWeights.put("foo", new BigDecimal("2.3"));

        BigDecimal actualCalculateResult = marketIndexCalculator.calculate(weightedPrices, marketWeights);

        assertEquals(new BigDecimal("5.29"), actualCalculateResult);
    }

    @Test
    void testCalculate4() {
        HashMap<String, BigDecimal> weightedPrices = new HashMap<>();
        weightedPrices.put("foo", new BigDecimal("2.3"));

        HashMap<String, BigDecimal> marketWeights = new HashMap<>();
        marketWeights.put("foo", null);

        assertThrows(IllegalArgumentException.class, () -> marketIndexCalculator.calculate(weightedPrices, marketWeights));
    }
}
