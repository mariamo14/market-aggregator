package com.market.aggregator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class MarketAggregatorApplicationTests {

	@Test
	void contextLoads() {
		// Context loading test
	}

	@Test
	void testRunApplicationWithDefaultFile(CapturedOutput output) {
		MarketAggregatorApplication.main(new String[]{});
		// Verify that processing completed message is logged.
		assertTrue(output.getOut().contains("Processing completed"),
				"Expected output to contain 'Processing completed'");
	}

	@Test
	void testRunApplicationWithExternalFile(CapturedOutput output, @TempDir Path tempDir) throws IOException {
		// Create a temporary market_log file with minimal valid content.
		Path tempLogFile = tempDir.resolve("temp_market_log.txt");
		String sampleLog = "2025-01-20 09:00:01;ABC;100;500\n2025-01-20 09:20:05;ABC;105;600";
		Files.writeString(tempLogFile, sampleLog);

		// Run the application with the external file path.
		MarketAggregatorApplication.main(new String[]{tempLogFile.toAbsolutePath().toString()});

		// Verify that processing completed message is logged.
		assertTrue(output.getOut().contains("Processing completed"),
				"Expected output to contain 'Processing completed'");
	}
}