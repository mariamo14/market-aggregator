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

@ContextConfiguration(classes = {FileMarketWeightsParser.class})
@ExtendWith(SpringExtension.class)
class FileMarketWeightsParserTest {
    @Autowired
    private FileMarketWeightsParser fileMarketWeightsParser;

    @Test
    void testParseMarketWeights() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> fileMarketWeightsParser.parseMarketWeights(new ByteArrayInputStream("AXAXAXAX".getBytes(StandardCharsets.UTF_8))));
        assertTrue(fileMarketWeightsParser.parseMarketWeights(new ByteArrayInputStream(new byte[]{})).isEmpty());
    }
}
