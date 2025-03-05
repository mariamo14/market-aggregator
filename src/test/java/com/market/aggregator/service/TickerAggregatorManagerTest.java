package com.market.aggregator.service;

import com.market.aggregator.domain.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {TickerAggregatorManager.class})
@ExtendWith(SpringExtension.class)
class TickerAggregatorManagerTest {
    @Autowired
    private TickerAggregatorManager tickerAggregatorManager;

    @Test
    @DisplayName("Test recordTrade(Trade)")
    void testRecordTrade() {
        Trade.TradeBuilder builderResult = Trade.builder();
        Trade.TradeBuilder tickerResult = builderResult.price(new BigDecimal("2.3")).quantity(1).ticker("Ticker");
        LocalDate ofResult = LocalDate.of(1970, 1, 1);
        Trade trade = tickerResult.timestamp(ofResult.atStartOfDay()).build();

        tickerAggregatorManager.recordTrade(trade);

        assertEquals(1, tickerAggregatorManager.getAggregationDates().size());
        assertSame(ofResult, tickerAggregatorManager.getFirstAggregationDate());
        assertSame(ofResult, tickerAggregatorManager.getLastAggregationDate());
    }

    @Test
    @DisplayName("Test recordTrade(Trade); given builder price BigDecimal(String) with '2.3'; then calls price(BigDecimal)")
    void testRecordTrade_givenBuilderPriceBigDecimalWith23_thenCallsPrice() {
        Trade.TradeBuilder builderResult = Trade.builder();
        builderResult.price(new BigDecimal("2.3"));
        Trade.TradeBuilder tradeBuilder = mock(Trade.TradeBuilder.class);
        when(tradeBuilder.price(Mockito.any())).thenReturn(builderResult);
        Trade.TradeBuilder tickerResult = tradeBuilder.price(new BigDecimal("2.3")).quantity(1).ticker("Ticker");
        LocalDate ofResult = LocalDate.of(1970, 1, 1);
        Trade trade = tickerResult.timestamp(ofResult.atStartOfDay()).build();

        tickerAggregatorManager.recordTrade(trade);

        verify(tradeBuilder).price(isA(BigDecimal.class));
        assertEquals(1, tickerAggregatorManager.getAggregationDates().size());
        assertSame(ofResult, tickerAggregatorManager.getFirstAggregationDate());
        assertSame(ofResult, tickerAggregatorManager.getLastAggregationDate());
    }

    @Test
    @DisplayName("Test getAggregationFor(LocalDate)")
    void testGetAggregationFor() {
        assertTrue(tickerAggregatorManager.getAggregationFor(LocalDate.of(1970, 1, 1)).isEmpty());
    }

    @Test
    @DisplayName("Test getFirstAggregationDate()")
    void testGetFirstAggregationDate() {
        assertThrows(IllegalStateException.class, () -> tickerAggregatorManager.getFirstAggregationDate());
    }

    @Test
    @DisplayName("Test getLastAggregationDate()")
    void testGetLastAggregationDate() {
        assertThrows(IllegalStateException.class, () -> tickerAggregatorManager.getLastAggregationDate());
    }

    @Test
    @DisplayName("Test getAggregationDates()")
    void testGetAggregationDates() {
        assertTrue(tickerAggregatorManager.getAggregationDates().isEmpty());
    }
}
