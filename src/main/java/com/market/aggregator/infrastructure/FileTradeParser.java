package com.market.aggregator.infrastructure;

import com.market.aggregator.domain.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

//The system begins by parsing the market log file with the specified format:
//"date+time;company ticker;price;number of securities traded", which happens in this class

@Component
public class FileTradeParser {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(FileTradeParser.class);

    // Parses the trades from an input stream.
    public List<Trade> parseTrades(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()//This is used to read the lines from the file
                    .map(String::trim) //Trim the lines
                    .filter(this::isValidLine)//Filter the valid lines
                    .map(this::parseTrade)//Map the lines to trades
                    .collect(Collectors.toList()); //Collect the trades to a list
                    //The Collection to a list might be a performance issue, we can refactor this to a better
                    //implementation in the future
        }
    }

    // Checks if a line is valid for parsing.
    private boolean isValidLine(String line) {
        //Check if the line is not empty, does not start with # and does not start with date
        //We can potentially add more checks here
        return !line.isEmpty() && !line.startsWith("#") && !line.toLowerCase().startsWith("date");
    }

    // Parses a line of trade data into a Trade object.
    private Trade parseTrade(String line) {
        String[] parts = line.split(";");
        if (parts.length < 4) {
            //Save out the lines where the ticker is not valid to allow logging of there are errors
            log.error("Invalid line format: {}", line);
            throw new IllegalArgumentException("Invalid line format: " + line);
        }

        //Log the amount of lines parsed
        log.info("Parsed line: {}", line);

        //Parse the line into a Trade object.
        LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim(), DATE_TIME_FORMATTER);
        String ticker = parts[1].trim();
        BigDecimal price = new BigDecimal(parts[2].trim());
        int quantity = Integer.parseInt(parts[3].trim());
        return Trade.builder() //Build the trade based on the parsed values
                .timestamp(timestamp)
                .ticker(ticker)
                .price(price)
                .quantity(quantity)
                .build();
    }
}