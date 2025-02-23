# Requirements Document

## Overview

The Market Aggregator application processes market trade log data to compute daily statistics for each traded ticker and
a weighted market index.

## Business Requirements

1. **Trade Data Processing:**
    - Input file format: `date+time;ticker;price;number of securities traded`
    - For each day and for each ticker, compute:
        - **Open Price:** Price of the first trade of the day.
        - **Close Price:** Price of the last trade of the day.
        - **High Price:** Maximum trade price of the day.
        - **Low Price:** Minimum trade price of the day.
        - **Volume:** Sum of (price * quantity).

2. **Market Index Calculation:**
    - Use a weighted sum of close prices for selected tickers:
        - ABC: 0.1
        - MEGA: 0.3
        - NGL: 0.4
        - TRX: 0.2
    - If a weighted ticker did not trade on a day, use the last known price; if missing entirely (first day), report "
      N/A".

3. **Error Handling:**
    - Malformed lines should be logged and ignored.
    - Missing data for non-weighted tickers should display "N/A" for prices and 0 for volume.

4. **Logging and Debugging:**
    - Provide comprehensive logging to track input parsing, trade recording, and market index calculation.
    - Exceptions should be logged with sufficient detail to assist with debugging and analysis.

5. **Test Coverage:**
    - Include unit tests for parsing and calculation components.
    - Provide integration tests to ensure that trade processing and aggregation are functioning correctly.


- **User Story 1:**
  *As a system user, I want the application to process a market log file and display daily aggregates for each ticker.*

- **User Story 2:**
  *As a system user, I want to see the open, close, high, and low prices along with the traded volume for each ticker
  for every trading day to analyze market performance.*

- **User Story 3:**
  *As a system user, I want the application to compute a weighted market index based on specific tickers so that I can
  quickly gauge overall market performance.*

## Assumptions

- The file format is consistent and uses a semicolon (`;`) as a delimiter.
- The system handles malformed lines gracefully (ignoring them while logging warnings).
- For tickers without trades on a particular day, the system outputs "N/A" for prices and 0 for volume.

## Non-functional Requirements

- **Modularity:** Code should be structured following SOLID principles.
- **Testability:** The system should be fully testable (unit, integration, and BDD).
- **Extensibility:** Designed to allow future integration into a microservices architecture.
- **Performance:** The system should handle large trade logs efficiently.
- **Error Handling:** Gracefully handle exceptions and log errors for debugging.
- **Logging:** Use logging to track system behavior and errors.

## Assumptions & Constraints

- The input file format is consistent.
- The system is built as a Spring Boot microservice.
- Future enhancements may include a REST API and a GUI.
