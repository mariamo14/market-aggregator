package com.market.aggregator.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {FileTradeParser.class})
@ExtendWith(SpringExtension.class)
class FileTradeParserTest {
    @Autowired
    private FileTradeParser fileTradeParser;

    @Test
    void testParseTrades() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> fileTradeParser.parseTrades(new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8))));
        assertTrue(fileTradeParser.parseTrades(new ByteArrayInputStream(new byte[]{})).isEmpty());
    }
}
