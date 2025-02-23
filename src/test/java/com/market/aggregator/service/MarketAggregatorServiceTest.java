package com.market.aggregator.service;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.infrastructure.FileMarketWeightsParser;
import com.market.aggregator.infrastructure.FileTradeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {MarketAggregatorService.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class MarketAggregatorServiceTest {
    @MockBean
    private FileMarketWeightsParser fileMarketWeightsParser;

    @MockBean
    private FileTradeParser fileTradeParser;

    @Autowired
    private MarketAggregatorService marketAggregatorService;

    @MockBean
    private MarketIndexCalculator marketIndexCalculator;

    @MockBean
    private TickerAggregatorManager tickerAggregatorManager;

    @Test
    @DisplayName("Test getAggregationFor(LocalDate)")
    void testGetAggregationFor() {

        when(tickerAggregatorManager.getAggregationFor(Mockito.any())).thenReturn(new HashMap<>());

        Map<String, AggregationRecord> actualAggregationFor = marketAggregatorService.getAggregationFor(LocalDate.of(1970, 1, 1));

        verify(tickerAggregatorManager).getAggregationFor(isA(LocalDate.class));
        assertTrue(actualAggregationFor.isEmpty());
    }

    @Test
    @DisplayName("Test processTrades(InputStream, InputStream); given HashMap() 'foo' is BigDecimal(String) with '2.3'; then calls getAggregationFor(LocalDate)")
    void testProcessTrades_givenHashMapFooIsBigDecimalWith23_thenCallsGetAggregationFor() throws IOException {

        when(fileTradeParser.parseTrades(Mockito.any())).thenReturn(new ArrayList<>());

        HashMap<String, BigDecimal> stringBigDecimalMap = new HashMap<>();
        stringBigDecimalMap.put("foo", new BigDecimal("2.3"));
        when(fileMarketWeightsParser.parseMarketWeights(Mockito.any())).thenReturn(stringBigDecimalMap);

        HashSet<LocalDate> localDateSet = new HashSet<>();
        localDateSet.add(LocalDate.of(1970, 1, 1));
        when(tickerAggregatorManager.getAggregationFor(Mockito.any())).thenReturn(new HashMap<>());
        when(tickerAggregatorManager.getAggregationDates()).thenReturn(localDateSet);
        ByteArrayInputStream tradesFile = new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8));

        marketAggregatorService.processTrades(tradesFile, new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8)));

        verify(fileMarketWeightsParser).parseMarketWeights(isA(InputStream.class));
        verify(fileTradeParser).parseTrades(isA(InputStream.class));
        verify(tickerAggregatorManager).getAggregationDates();
        verify(tickerAggregatorManager, atLeast(1)).getAggregationFor(isA(LocalDate.class));
    }

    @Test
    @DisplayName("Test processTrades(InputStream, InputStream)")
    void testProcessTrades() throws IOException {

        when(fileTradeParser.parseTrades(Mockito.any())).thenReturn(new ArrayList<>());
        when(fileMarketWeightsParser.parseMarketWeights(Mockito.any())).thenReturn(new HashMap<>());
        when(marketIndexCalculator.calculate(Mockito.any(), Mockito.any())).thenReturn(new BigDecimal("2.3"));

        HashSet<LocalDate> localDateSet = new HashSet<>();
        localDateSet.add(LocalDate.of(1970, 1, 1));

        HashMap<String, AggregationRecord> stringAggregationRecordMap = new HashMap<>();
        stringAggregationRecordMap.put("Index for %s: %.1f%n", AggregationRecord.of("Index for %s: %.1f%n"));
        when(tickerAggregatorManager.getAggregationFor(Mockito.any())).thenReturn(stringAggregationRecordMap);
        when(tickerAggregatorManager.getAggregationDates()).thenReturn(localDateSet);
        ByteArrayInputStream tradesFile = new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8));

        marketAggregatorService.processTrades(tradesFile, new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8)));

        verify(fileMarketWeightsParser).parseMarketWeights(isA(InputStream.class));
        verify(fileTradeParser).parseTrades(isA(InputStream.class));
        verify(marketIndexCalculator).calculate(isA(Map.class), isA(Map.class));
        verify(tickerAggregatorManager).getAggregationDates();
        verify(tickerAggregatorManager, atLeast(1)).getAggregationFor(isA(LocalDate.class));
    }


    @Test
    @DisplayName("Test processTrades(InputStream, InputStream); given HashMap() 'foo' is AggregationRecord with stockName is 'Index for %s: N/A%n'")
    void testProcessTrades_givenHashMapFooIsAggregationRecordWithStockNameIsIndexForSNAN() throws IOException {

        when(fileTradeParser.parseTrades(Mockito.any())).thenReturn(new ArrayList<>());

        HashMap<String, BigDecimal> stringBigDecimalMap = new HashMap<>();
        stringBigDecimalMap.put("foo", new BigDecimal("2.3"));
        when(fileMarketWeightsParser.parseMarketWeights(Mockito.any())).thenReturn(stringBigDecimalMap);

        HashSet<LocalDate> localDateSet = new HashSet<>();
        localDateSet.add(LocalDate.of(1970, 1, 1));

        HashMap<String, AggregationRecord> stringAggregationRecordMap = new HashMap<>();
        stringAggregationRecordMap.put("foo", AggregationRecord.of("Index for %s: N/A%n"));
        when(tickerAggregatorManager.getAggregationFor(Mockito.any())).thenReturn(stringAggregationRecordMap);
        when(tickerAggregatorManager.getAggregationDates()).thenReturn(localDateSet);
        ByteArrayInputStream tradesFile = new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8));

        marketAggregatorService.processTrades(tradesFile, new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8)));

        verify(fileMarketWeightsParser).parseMarketWeights(isA(InputStream.class));
        verify(fileTradeParser).parseTrades(isA(InputStream.class));
        verify(tickerAggregatorManager).getAggregationDates();
        verify(tickerAggregatorManager, atLeast(1)).getAggregationFor(isA(LocalDate.class));
    }


    @Test
    @DisplayName("Test processTrades(InputStream, InputStream); given MarketIndexCalculator; then calls parseMarketWeights(InputStream)")
    void testProcessTrades_givenMarketIndexCalculator_thenCallsParseMarketWeights() throws IOException {

        when(fileTradeParser.parseTrades(Mockito.any())).thenReturn(new ArrayList<>());
        when(fileMarketWeightsParser.parseMarketWeights(Mockito.any())).thenReturn(new HashMap<>());
        when(tickerAggregatorManager.getAggregationDates()).thenReturn(new HashSet<>());
        ByteArrayInputStream tradesFile = new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8));

        marketAggregatorService.processTrades(tradesFile, new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8)));

        verify(fileMarketWeightsParser).parseMarketWeights(isA(InputStream.class));
        verify(fileTradeParser).parseTrades(isA(InputStream.class));
        verify(tickerAggregatorManager).getAggregationDates();
    }

    @Test
    @DisplayName("Test processTrades(InputStream, InputStream); given MarketIndexCalculator; then calls parseMarketWeights(InputStream)")
    void testProcessTrades_givenMarketIndexCalculator_thenCallsParseMarketWeights2() throws IOException {

        when(fileTradeParser.parseTrades(Mockito.any())).thenReturn(new ArrayList<>());

        HashMap<String, BigDecimal> stringBigDecimalMap = new HashMap<>();
        stringBigDecimalMap.put("foo", new BigDecimal("2.3"));
        when(fileMarketWeightsParser.parseMarketWeights(Mockito.any())).thenReturn(stringBigDecimalMap);
        when(tickerAggregatorManager.getAggregationDates()).thenReturn(new HashSet<>());
        ByteArrayInputStream tradesFile = new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8));

        marketAggregatorService.processTrades(tradesFile, new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8)));

        verify(fileMarketWeightsParser).parseMarketWeights(isA(InputStream.class));
        verify(fileTradeParser).parseTrades(isA(InputStream.class));
        verify(tickerAggregatorManager).getAggregationDates();
    }

    @Test
    @DisplayName("Test processTrades(InputStream, InputStream); then calls calculate(Map, Map)")
    void testProcessTrades_thenCallsCalculate() throws IOException {

        when(fileTradeParser.parseTrades(Mockito.any())).thenReturn(new ArrayList<>());
        when(fileMarketWeightsParser.parseMarketWeights(Mockito.any())).thenReturn(new HashMap<>());
        when(marketIndexCalculator.calculate(Mockito.any(), Mockito.any())).thenReturn(new BigDecimal("2.3"));

        HashSet<LocalDate> localDateSet = new HashSet<>();
        localDateSet.add(LocalDate.of(1970, 1, 1));
        when(tickerAggregatorManager.getAggregationFor(Mockito.any())).thenReturn(new HashMap<>());
        when(tickerAggregatorManager.getAggregationDates()).thenReturn(localDateSet);
        ByteArrayInputStream tradesFile = new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8));

        marketAggregatorService.processTrades(tradesFile, new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8)));

        verify(fileMarketWeightsParser).parseMarketWeights(isA(InputStream.class));
        verify(fileTradeParser).parseTrades(isA(InputStream.class));
        verify(marketIndexCalculator).calculate(isA(Map.class), isA(Map.class));
        verify(tickerAggregatorManager).getAggregationDates();
        verify(tickerAggregatorManager, atLeast(1)).getAggregationFor(isA(LocalDate.class));
    }

    @Test
    @DisplayName("Test getAggregationDates()")
    void testGetAggregationDates() {
        when(tickerAggregatorManager.getAggregationDates()).thenReturn(new HashSet<>());

        Set<LocalDate> actualAggregationDates = marketAggregatorService.getAggregationDates();

        verify(tickerAggregatorManager).getAggregationDates();
        assertTrue(actualAggregationDates.isEmpty());
    }
}
