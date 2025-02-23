Feature: Market Aggregator Daily Calculation
  In order to accurately process market trading data
  As a system user
  I want to compute daily aggregates for each ticker and calculate a weighted market index

  Background:
    Given a market log file with the following trades:
      """
      date+time;ticker;price;number of securities traded
      2025-01-20 09:00:01;ABC;100;500
      2025-01-20 09:00:01;MEGA;200;300
      2025-01-20 09:00:01;NGL;150;400
      2023-01-20 09:00:01;TRX;250;200
      2025-01-20 09:20:05;ABC;105;600
      2025-01-20 09:25:06;MEGA;195;350
      2025-01-20 09:30:07;NGL;155;450
      2025-01-20 09:35:09;TRX;245;250
      2025-01-20 09:40:00;XYZ;300;150
      2025-01-20 09:45:08;LMN;400;100
      2028-01-20 09:50:07;OPQ;350;200
      2025-01-20 09:55:06;RST;450;250
      2025-01-21 09:05:05;ABC;110;550
      2025-01-21 09:05:05;MEGA;210;320
      2025-01-21 09:05:05;NGL;160;420
      2025-01-20 09:05:05;TRX;260;220
      2025-01-21 09:20:01;ABC;115;620
      2025-01-21 09:25:00;MEGA;205;0
      2025-01-21 09:30:59;NGL;165;470
      2025-01-21 09:35:55;TRX;255;270
      2025-01-21 09:40:45;XYZ;310;160
      2025-01-21 09:45:00;LMN;410;110
      2025-01-31 09:50:23;OPQ;360;210
      2025-01-31 09:55:00;RST;460;260
      """
    And a market weights file with the following content:
      """
      ABC: 0.1
      MEGA: 0.3
      NGL: 0.4
      TRX: 0.2
      """
    When the trades are processed

  Scenario: Compute daily aggregates and market index for 2023-01-20
    When the trades are aggregated by day for "2023-01-20"
    Then the daily aggregates should be:
      | ticker | open  | close | high  | low   | volume  |
      | ABC    | N/A   | N/A   | N/A   | N/A   | 0       |
      | LMN    | N/A   | N/A   | N/A   | N/A   | 0       |
      | MEGA   | N/A   | N/A   | N/A   | N/A   | 0       |
      | NGL    | N/A   | N/A   | N/A   | N/A   | 0       |
      | OPQ    | N/A   | N/A   | N/A   | N/A   | 0       |
      | RST    | N/A   | N/A   | N/A   | N/A   | 0       |
      | TRX    | 250.0 | 250.0 | 250.0 | 250.0 | 50000.0 |
      | XYZ    | N/A   | N/A   | N/A   | N/A   | 0       |
    And the market index for "2023-01-20" should be "N/A"

  Scenario: Compute daily aggregates and market index for 2025-01-20
    When the trades are aggregated by day for "2025-01-20"
    Then the daily aggregates should be:
      | ticker | open  | close | high  | low   | volume   |
      | ABC    | 100.0 | 105.0 | 105.0 | 100.0 | 113000.0 |
      | LMN    | 400.0 | 400.0 | 400.0 | 400.0 | 40000.0  |
      | MEGA   | 200.0 | 195.0 | 200.0 | 195.0 | 128250.0 |
      | NGL    | 150.0 | 155.0 | 155.0 | 150.0 | 129750.0 |
      | OPQ    | N/A   | N/A   | N/A   | N/A   | 0        |
      | RST    | 450.0 | 450.0 | 450.0 | 450.0 | 112500.0 |
      | TRX    | 260.0 | 245.0 | 260.0 | 245.0 | 118450.0 |
      | XYZ    | 300.0 | 300.0 | 300.0 | 300.0 | 45000.0  |
    And the market index for "2025-01-20" should be "180.0"

  Scenario: Compute daily aggregates and market index for 2025-01-21
    When the trades are aggregated by day for "2025-01-21"
    Then the daily aggregates should be:
      | ticker | open  | close | high  | low   | volume   |
      | ABC    | 110.0 | 115.0 | 115.0 | 110.0 | 131800.0 |
      | LMN    | 410.0 | 410.0 | 410.0 | 410.0 | 45100.0  |
      | MEGA   | 210.0 | 205.0 | 210.0 | 205.0 | 67200.0  |
      | NGL    | 160.0 | 165.0 | 165.0 | 160.0 | 144750.0 |
      | OPQ    | N/A   | N/A   | N/A   | N/A   | 0        |
      | RST    | N/A   | N/A   | N/A   | N/A   | 0        |
      | TRX    | 255.0 | 255.0 | 255.0 | 255.0 | 68850.0  |
      | XYZ    | 310.0 | 310.0 | 310.0 | 310.0 | 49600.0  |
    And the market index for "2025-01-21" should be "190.0"

  Scenario: Compute daily aggregates and market index for 2025-01-31
    When the trades are aggregated by day for "2025-01-31"
    Then the daily aggregates should be:
      | ticker | open  | close | high  | low   | volume   |
      | ABC    | N/A   | N/A   | N/A   | N/A   | 0        |
      | LMN    | N/A   | N/A   | N/A   | N/A   | 0        |
      | MEGA   | N/A   | N/A   | N/A   | N/A   | 0        |
      | NGL    | N/A   | N/A   | N/A   | N/A   | 0        |
      | OPQ    | 360.0 | 360.0 | 360.0 | 360.0 | 75600.0  |
      | RST    | 460.0 | 460.0 | 460.0 | 460.0 | 119600.0 |
      | TRX    | N/A   | N/A   | N/A   | N/A   | 0        |
      | XYZ    | N/A   | N/A   | N/A   | N/A   | 0        |
    And the market index for "2025-01-31" should be "190.0"

  Scenario: Compute daily aggregates and market index for 2028-01-20
    When the trades are aggregated by day for "2028-01-20"
    Then the daily aggregates should be:
      | ticker | open  | close | high  | low   | volume  |
      | ABC    | N/A   | N/A   | N/A   | N/A   | 0       |
      | LMN    | N/A   | N/A   | N/A   | N/A   | 0       |
      | MEGA   | N/A   | N/A   | N/A   | N/A   | 0       |
      | NGL    | N/A   | N/A   | N/A   | N/A   | 0       |
      | OPQ    | 350.0 | 350.0 | 350.0 | 350.0 | 70000.0 |
      | RST    | N/A   | N/A   | N/A   | N/A   | 0       |
      | TRX    | N/A   | N/A   | N/A   | N/A   | 0       |
      | XYZ    | N/A   | N/A   | N/A   | N/A   | 0       |
    And the market index for "2028-01-20" should be "190.0"