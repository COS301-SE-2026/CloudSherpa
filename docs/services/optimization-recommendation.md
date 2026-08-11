# CloudSherpa Optimization Recommendation System

## Overall Optimization Recommendation Architecture

The **CloudSherpa Optimization Recommendation** Engine employs an asynchronous, batch-processing architecture designed to decouple heavy analytical queries from synchronous user interactions.

This system begins its lifecycle directly at the database tier. It reads from the existing normalized data, processes it in the background, and outputs finalized, highly performant records for the dashboard to consume.

```mermaid
flowchart TD
    %% Starting Point: Existing Normalized Data
    subgraph Input [Existing Normalized Data Layer]
        DB_METRICS[(nnormalized_metrics)]
        DB_RES[(resources & normalized_costs)]
        DB_CAT[(pricing & rules catalogs)]
    end

    subgraph Worker [Asynchronous Optimization Worker]
        AGGREGATOR[fa:fa-calculator Statistics Aggregator\nCompresses raw time-series into baselines]
        RULE_ENGINE[fa:fa-gavel Rule Evaluation Engine\nMatches baselines against cost-saving rules]
        RESOLVER[fa:fa-filter Conflict Resolver\nApplies hierarchy & safety policies]
    end

    subgraph Output [Output & Delivery]
        DB_STATS[(SherpaDB\noptimization_metric_statistics)]
        DB_REC[(SherpaDB\noptimization_recommendation)]
        API[Service API\nServes pre-calculated results]
    end

    DB_METRICS -->|1. Read Unprocessed Data| AGGREGATOR
    AGGREGATOR -->|2. Persist 7d/30d Stats| DB_STATS
    
    DB_STATS -->|3. Load Summaries| RULE_ENGINE
    DB_RES -->|4. Load Targets| RULE_ENGINE
    DB_CAT -->|5. Load Pricing| RULE_ENGINE
    
    RULE_ENGINE -->|6. Draft Candidates| RESOLVER
    RESOLVER -->|7. Persist Final Decision| DB_REC
    
    DB_REC -->|8. Query ACTIVE records| API
```

## Component Responsibilities

### Asynchronous Optimization Worker

This is a scheduled background process responsible for all heavy lifting:

- **Reading**: Pulls unprocessed normalized metrics from SherpaDB based on a durable processing watermark.
- **Calculating**: Computes heavy statistical summaries (percentiles, standard deviations) and saves them to SherpaDB.
- **Evaluating**: Reads pricing and resource catalogs, then runs the generated statistics against optimization rules.
- **Resolving**: Filters out duplicate or mutually exclusive actions (e.g., choosing "Terminate" over "Downsize") and checks safety policies.
- **Persisting**: Writes the final, resolved recommendation (including the mathematical evidence and estimated savings) to the database.

### Service Application

The API layer acts as a lightweight delivery mechanism.

- **Reading**: Queries the optimization_recommendation table for active records.
- **State Management**: Updates the status of a recommendation when a user interacts with it (e.g., changing status to ACKNOWLEDGED or DISMISSED).
- **Constraint**: The API must not calculate P95, standard deviations, or any other expensive statistics during a request.

### Database

- **SherpaDB**: It provides the input context (`resources`, `normalized_costs`, `catalogs`) and stores the output artifacts (`optimization_metric_statistics`, `optimization_recommendation`, processing watermarks).

### Dashboard

- **Display**: Renders the active recommendations and their estimated financial savings.
- **Evidence Visualization**: Displays the underlying evidence (e.g., showing the user that their P95 CPU was only 12%) to build trust in the automated recommendation.
- **Actionable Controls**: Allows the user to acknowledge or dismiss recommendations.

## Statistical Aggregation Approach

The statistical aggregation approach is designed to prevent system timeouts by moving all complex math out of the user's critical path. It uses a watermark-based processing strategy.

- **The Watermark Strategy**: The Optimization Scheduler relies on a durable `processing_watermark` table. When the scheduler wakes up, it checks this table to see exactly when it last successfully processed data for a specific tenant.

- **Database-Native Calculation**: Whenever possible, statistical functions (like averages and max values) are pushed down to the database level using native SQL analytical functions, rather than pulling millions of raw rows into the application's memory heap.

- **Persistent Storage**: The resulting summaries are upserted into the `optimization_metric_statistics` table. The Rule Engine will only ever query this summary table, never the raw metrics tables.

## Required Statistical Windows and Metrics

To prevent false positives (like recommending a server be downsized just because it had a quiet weekend), the engine requires long-term context and data quality checks.

### Statistical Windows

Statistics are calculated over two distinct rolling windows to capture both immediate spikes and long-term trends:

- **7-Day Window (7d)**: Used to detect short-term maximums, recent usage spikes, and immediate behavioral changes.
- **30-Day Window (30d)**: Used to establish reliable, long-term operational baselines and accurate monthly cost projections.

### Required Metrics
For every combination of Tenant + Resource + Canonical Metric + Window, the aggregator calculates and stores the following fields:

**Core Distribution Metrics**

- **minimum_value & maximum_value**: The absolute floor and ceiling of the resource's usage.
- **average_value & median_value**: The general, day-to-day operational baseline.
- **p95_value & p99_value (Percentiles)**: Crucial for safe recommendations. P95 strips out the top 5% of usage spikes (like brief CPU spikes during a reboot). Sizing a server based on P95 ensures it can handle sustained heavy load without over-provisioning for rare anomalies.
- **standard_deviation**: Measures the volatility of the workload. A highly erratic workload (high deviation) is riskier to downsize than a perfectly flat, consistent workload (low deviation).

**Anomaly Metrics**

- **spike_count**: How many times usage breached a defined maximum threshold.
- **peak_duration_seconds**: The total continuous time the resource spent maxed out.