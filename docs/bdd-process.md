# BDD Process for Market Aggregator

## Approach
1. **Define Requirements:**  
   Capture business requirements (as seen in `requirements.md`) in plain language.

2. **Write Feature Files:**  
   Create Gherkin feature files to describe the expected behavior. For example, scenarios for:
    - Correct aggregation when all weighted tickers are present.
    - Handling missing weighted tickers.
    - Processing malformed input gracefully.

3. **Implement Step Definitions:**  
   Map each Gherkin step to Java code in step definitions. This guides the development process.

4. **Iterative Development:**
    - Write a failing test
    - Implement minimal code to pass the test
    - Refactor as needed.
    - Repeat for each new scenario.

## Benefits
- Provides living documentation.
- Ensures the system meets business requirements.
- Facilitates early testing and continuous delivery.
