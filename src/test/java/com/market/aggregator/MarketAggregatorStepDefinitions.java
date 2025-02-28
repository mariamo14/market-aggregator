package com.market.aggregator;

import com.market.aggregator.domain.AggregationRecord;
import com.market.aggregator.infrastructure.FileMarketWeightsParser;
import com.market.aggregator.infrastructure.FileTradeParser;
import com.market.aggregator.service.MarketAggregatorService;
import com.market.aggregator.service.MarketIndexCalculator;
import com.market.aggregator.service.TickerAggregatorManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MarketAggregatorStepDefinitions {

    private String tradesContent;
    private String weightsContent;
    private Map<String, AggregationRecord> aggregationResult;
    private MarketAggregatorService aggregatorService;

    @Given("a market log file with the following trades:")
    public void givenMarketLogFileWithTheFollowingTrades(String fileContent) {
        tradesContent = fileContent;
    }

    @Given("a market weights file with the following content:")
    public void givenMarketWeightsFileWithTheFollowingContent(String fileContent) {
        weightsContent = fileContent;
    }

    @When("the trades are processed")
    public void whenTheTradesAreProcessed() throws Exception {
        FileTradeParser tradeParser = new FileTradeParser();
        FileMarketWeightsParser weightsParser = new FileMarketWeightsParser();
        MarketIndexCalculator calculator = new MarketIndexCalculator();
        TickerAggregatorManager tickerManager = new TickerAggregatorManager();
        aggregatorService = new MarketAggregatorService(tradeParser, weightsParser, calculator, tickerManager);

        ByteArrayInputStream tradesStream = new ByteArrayInputStream(tradesContent.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream weightsStream = new ByteArrayInputStream(weightsContent.getBytes(StandardCharsets.UTF_8));
        aggregatorService.processTrades(tradesStream, weightsStream);
    }

    @When("the trades are aggregated by day for {string}")
    public void whenTheTradesAreAggregatedByDayFor(String targetDateStr) {
        LocalDate targetDate = LocalDate.parse(targetDateStr);
        aggregationResult = aggregatorService.getAggregationFor(targetDate);
    }

    @Then("the daily aggregates should be:")
    public void thenTheDailyAggregatesShouldBe(DataTable table) {
        table.asMaps(String.class, String.class).forEach(expectedRow -> {
            String ticker = expectedRow.get("ticker");
            String openStr = expectedRow.get("open");
            AggregationRecord record = aggregationResult.get(ticker);
            if ("N/A".equals(openStr)) {
                assertNull(record, "Expected no aggregation record for ticker: " + ticker);
            } else {
                BigDecimal expectedOpen = new BigDecimal(expectedRow.get("open"));
                BigDecimal expectedClose = new BigDecimal(expectedRow.get("close"));
                BigDecimal expectedHigh = new BigDecimal(expectedRow.get("high"));
                BigDecimal expectedLow = new BigDecimal(expectedRow.get("low"));
                BigDecimal expectedVolume = new BigDecimal(expectedRow.get("volume"));
                System.out.printf("Ticker: %s, Expected Close: %s, Actual Close: %s%n", ticker, expectedClose, record.getClosePrice());
                assertEquals(0, expectedOpen.compareTo(record.getOpenPrice()), "Open price mismatch for ticker: " + ticker);
                assertEquals(0, expectedClose.compareTo(record.getClosePrice()), "Close price mismatch for ticker: " + ticker);
                assertEquals(0, expectedHigh.compareTo(record.getHighestPrice()), "High price mismatch for ticker: " + ticker);
                assertEquals(0, expectedLow.compareTo(record.getLowestPrice()), "Low price mismatch for ticker: " + ticker);
                assertEquals(0, expectedVolume.compareTo(record.getVolumeOfTrades()), "Volume mismatch for ticker: " + ticker);
            }
        });
    }

    @Then("the market index for {string} should be {string}")
    public void thenTheMarketIndexShouldBe(String targetDateStr, String expectedIndexStr) throws Exception {
        BigDecimal expected = "N/A".equals(expectedIndexStr) ? null : new BigDecimal(expectedIndexStr);
        LocalDate targetDate = LocalDate.parse(targetDateStr);

        // Parse market weights.
        FileMarketWeightsParser parser = new FileMarketWeightsParser();
        ByteArrayInputStream weightsStream = new ByteArrayInputStream(weightsContent.getBytes(StandardCharsets.UTF_8));
        Map<String, BigDecimal> marketWeights = parser.parseMarketWeights(weightsStream);

        MarketIndexCalculator calculator = new MarketIndexCalculator();
        BigDecimal actual = null;

        // Sorted dates in descending order, considering dates up to targetDate.
        List<LocalDate> sortedDates = new ArrayList<>(aggregatorService.getAggregationDates());
        Collections.sort(sortedDates, Collections.reverseOrder());

        for (LocalDate date : sortedDates) {
            if (date.isAfter(targetDate)) {
                continue;
            }
            Map<String, AggregationRecord> aggregationForDate = aggregatorService.getAggregationFor(date);
            Map<String, BigDecimal> weightedPrices = new HashMap<>();
            boolean completeForIndex = true;
            for (String ticker : marketWeights.keySet()) {
                AggregationRecord record = aggregationForDate.get(ticker);
                if (record != null && record.getClosePrice() != null) {
                    weightedPrices.put(ticker, record.getClosePrice());
                } else {
                    completeForIndex = false;
                    break;
                }
            }
            if (completeForIndex) {
                actual = calculator.calculate(weightedPrices, marketWeights);
                break;
            }
        }

        if (expected == null) {
            assertNull(actual, "Expected market index to be N/A");
        } else {
            assertEquals(0, expected.compareTo(actual), "Market index mismatch");
        }
    }
}