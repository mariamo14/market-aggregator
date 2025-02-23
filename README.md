# Market Aggregator

## Overview

Market Aggregator is an application that processes market trade logs to compute daily aggregates and a weighted market
index. The project follows Behavior-Driven Development (BDD) practices, ensuring that the solution meets business
requirements through comprehensive feature files and tests.

## Key Features

- **Trade Data Processing:**  
  Computes open, close, high, low prices, and volume for each ticker by day.
- **Market Index Calculation:**  
  Calculates a weighted index using predefined ticker weights.
- **Error Handling:**  
  Logs and skips malformed lines.
- **BDD Driven Development:**  
  Uses Gherkin feature files to drive development.
- **Modern Architecture:**  
  Built with Spring Boot, uses dependency injection, and is designed for extensibility.

## Architecture Overview

The application is implemented as a modular, Spring Boot-based microservice following a clear separation of concerns.
The core functionality is organized around domain models, service interfaces, and concrete implementations, ensuring
reusability and testability.

## Key Components

- **MarketAggregatorService:**  
  Processes trades and calculates the market index.
- **MarketIndexCalculator:**  
  Computes the weighted market index using the latest close prices.
- **FileTradeParser** and **FileMarketWeightsParser:**  
  Parse input files for trades and market weights.
- **TickerAggregatorManager:**  
  Maintains daily aggregation records.

## Building and Running

### Build
Use Maven to build the application:
mvn clean package

### Run
Run the application with:
java -jar target/market-aggregator-0.0.1.jar

## Testing
Run unit and integration tests with:
mvn test

### BDD Process for Market Aggregator
refer to `docs/bdd-process.md` for details on the BDD process.

## Future Considerations

- **REST API Integration:**  
  Plans to include a REST endpoint that will permit direct market data upload and JSON responses for aggregation
  results.
- **UI Enhancements:**  
  Further improvements are expected to expose a user-friendly interface for viewing aggregated market data.
