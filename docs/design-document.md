# Market Aggregator Design Document

## Architecture Overview

The application is implemented as a modular, Spring Boot-based microservice following a clear separation of concerns.
The core functionality is organized around domain models, service interfaces, and concrete implementations, ensuring
reusability and testability.

## Key Components

- **MarketAggregatorService:**  
  Handles trade processing, including parsing input files, recording trades, and calculating the market index based on
  weighted stock prices. Improved exception handling and logging have been integrated for better resilience and
  debugging.

- **MarketIndexCalculator:**  
  Calculates the weighted market index from the close prices and market weights. The design ensures that null values
  trigger meaningful exceptions.

- **FileTradeParser and FileMarketWeightsParser:**  
  Support various input formats and edge cases. These components have been updated to robustly differentiate between
  valid and malformed inputs.

- **TickerAggregatorManager:**  
  Maintains daily aggregation records and manages the mapping between dates and tickers. Enhancements ensure efficient
  retrieval of both first and last aggregation dates.

## Enhancements and Refactoring

- **Core Business Logic:**  
  Enhanced handling of trade aggregation and market index updating, ensuring that missing trade data is managed
  appropriately by using previous price values where necessary.

- **Exception Handling:**  
  Both index calculation and input parsing now include robust error detection, throwing precise exceptions when
  encountering null values or malformed data.

- **Logging and Debugging:**  
  Additional logging has been integrated across the application to provide a clear view of the execution flow and
  debugging information.

- **Test Coverage:**  
  Comprehensive unit and integration tests have been added or updated to cover scenarios, including various edge
  cases for file parsing and index calculation.

## Future Considerations

- **REST API Integration:**  
  Plans to include a REST endpoint that will permit direct market data upload and JSON responses for aggregation
  results.

- **UI Enhancements:**  
  Further improvements are expected to expose a user-friendly interface for viewing aggregated market data.