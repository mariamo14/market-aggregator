Feature: Market Aggregator Daily Calculation
  In order to accurately process market trading data
  As a system user
  I want to compute daily aggregates for each ticker and calculate a weighted market index

  Background:
    # This background sets up a complete market log file containing trades from multiple dates.
    Given a market log file with the following trades:
      """
      date+time;ticker;price;number of securities traded
      2025-01-20 09:00:01;ABC;100;500
      2025-01-20 09:00:01;MEGA;200;300
      2025-01-20 09:00:01;NGL;150;400
      2025-01-20 09:00:01;TRX;250;200
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

  Scenario: Compute daily aggregates and market index on a day with complete data
    When the trades are aggregated by day for "2025-01-20"
    Then the daily aggregates should be:
      | ticker | open  | close | high  | low   | volume          |
      | ABC    | 100.0 | 105.0 | 105.0 | 100.0 | 100*500+105*600 |
      | MEGA   | 200.0 | 195.0 | 200.0 | 195.0 | 200*300+195*350 |
      | NGL    | 150.0 | 155.0 | 155.0 | 150.0 | 150*400+155*450 |
      | TRX    | 250.0 | 245.0 | 250.0 | 245.0 | 250*200+245*250 |
      | XYZ    | 300.0 | 300.0 | 300.0 | 300.0 | 300*150         |
      | LMN    | 400.0 | 400.0 | 400.0 | 400.0 | 400*100         |
      | OPQ    | N/A   | N/A   | N/A   | N/A   | 0               |
      | RST    | N/A   | N/A   | N/A   | N/A   | 0               |
    And the market index should be computed as:
      | weighted_tickers | ABC | MEGA | NGL | TRX |
      | close prices     | 105 | 195  | 155 | 245 |
    And the index value should be 0.1*105 + 0.3*195 + 0.4*155 + 0.2*245

  Scenario: Compute daily aggregates on a day with missing trades for some tickers
    When the trades are aggregated by day for "2025-01-21"
    Then the daily aggregates should be:
      | ticker | open  | close | high  | low   | volume          |
      | ABC    | 110.0 | 115.0 | 115.0 | 110.0 | 110*550+115*620 |
      | MEGA   | 210.0 | 205.0 | 210.0 | 205.0 | 210*320+205*0   |
      | NGL    | 160.0 | 165.0 | 165.0 | 160.0 | 160*420+165*470 |
      | TRX    | 260.0 | 255.0 | 260.0 | 255.0 | 260*220+255*270 |
      | XYZ    | 310.0 | 310.0 | 310.0 | 310.0 | 310*160         |
      | LMN    | 410.0 | 410.0 | 410.0 | 410.0 | 410*110         |
      | OPQ    | N/A   | N/A   | N/A   | N/A   | 0               |
      | RST    | N/A   | N/A   | N/A   | N/A   | 0               |
    And the market index should be computed as:
      | weighted_tickers | ABC | MEGA | NGL | TRX |
      | close prices     | 115 | 205  | 165 | 255 |
    And the index value should be 0.1*115 + 0.3*205 + 0.4*165 + 0.2*255

  Scenario: Do not compute market index on the first day when weighted tickers are incomplete
    Given a market log file with the following trades:
      """
      date+time;ticker;price;number of securities traded
      2025-01-20 09:00:01;ABC;100;500
      2025-01-20 09:00:01;MEGA;200;300
      """
    When the trades are aggregated by day for "2025-01-20"
    Then the market index should be "N/A"

  Scenario: Reuse previous market index when weighted tickers are missing on a subsequent day
    Given on date "2025-01-22" the weighted ticker "TRX" did not trade
    And the last known market index is "175.0"
    When I process the log file for date "2025-01-22"
    Then the INDEX should be "175.0"