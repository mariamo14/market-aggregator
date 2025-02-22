package com.market.aggregator;

import com.market.aggregator.infrastructure.FileMarketWeightsParser;
import com.market.aggregator.infrastructure.FileTradeParser;
import com.market.aggregator.service.MarketAggregatorService;
import com.market.aggregator.service.MarketIndexCalculator;
import com.market.aggregator.service.TickerAggregatorManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@SpringBootApplication
@Slf4j
public class MarketAggregatorApplication implements CommandLineRunner {

//    private final MarketAggregatorService aggregatorService;
//
//    public MarketAggregatorApplication(MarketAggregatorService aggregatorService) {
//        this.aggregatorService = aggregatorService;
//    }
//

    public static void main(String[] args) {
        SpringApplication.run(MarketAggregatorApplication.class, args);
    }

    private InputStream getResourceAsStream(String resourceName) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourceName);
        if (!resource.exists()) {
            throw new IllegalArgumentException("File not found: " + resourceName);
        }
        return resource.getInputStream();
    }

    @Override
    public void run(String... args) throws Exception {
        Path filePath;
        if (args.length < 1) {
            // No command-line argument provided: automatically use the default file.
            try {
                File defaultFile = new ClassPathResource("market_log.txt").getFile();
                filePath = defaultFile.toPath();
                log.info("No file provided. Using default file: {}", filePath);
            } catch (Exception e) {
                log.error("Default file not found in resources.", e);
            }
        } else {
            filePath = Path.of(args[0]);
        }

        log.info("Starting Market Aggregator...");
        InputStream logStream = getResourceAsStream("market_log.txt");
        InputStream weightsStream = getResourceAsStream("market_weights.txt");
        log.info("Processing trades...");
        log.info(weightsStream.toString());

        FileTradeParser tradeParser = new FileTradeParser();
        FileMarketWeightsParser weightsParser = new FileMarketWeightsParser();
        MarketIndexCalculator marketIndexCalculator = new MarketIndexCalculator();
        TickerAggregatorManager tickerAggregatorManager = new TickerAggregatorManager();

        MarketAggregatorService aggregatorService = new MarketAggregatorService(tradeParser, weightsParser, marketIndexCalculator, tickerAggregatorManager);
        aggregatorService.processTrades(logStream, weightsStream);

    }
}
