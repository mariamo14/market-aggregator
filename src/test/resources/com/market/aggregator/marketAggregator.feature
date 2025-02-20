Feature: Market Aggregator Daily Calculation
  In order to accurately process market trading data
  As a system user
  I want to compute daily aggregates and calculate a weighted market index

  Scenario: Compute daily aggregates when all weighted tickers are present
    Given a market log file with the following trades:
      """
      date+time;ticker;price;number of securities traded
      2025-01-20 09:00:01;ABC;100;500
      2025-01-20 09:00:01;MEGA;200;300
      2025-01-20 09:00:01;NGL;150;400
      2025-01-20 09:00:01;TRX;250;200
      2025-01-20 09:20:05;ABC;105;600
      """
    When the trades are aggregated by day
    Then the daily aggregate for ticker "ABC" should have:
      | open   | 100.0 |
      | close  | 105.0 |
      | high   | 105.0 |
      | low    | 100.0 |
      | volume | 100*500+105*600 |
    And the market index should be computed correctly

  Scenario: Handle missing weighted ticker on the first day
    Given a market log file with the following trades:
      """
      date+time;ticker;price;number of securities traded
      2025-01-20 09:00:01;ABC;100;500
      2025-01-20 09:00:01;MEGA;200;300
      """
    When the trades are aggregated by day
    Then the market index should be "N/A"
