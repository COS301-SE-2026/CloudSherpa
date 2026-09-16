# Automated Alerts

**Overview**  
This document specifies a design for the Automated Alerts engine that reuses existing statistical aggregation (optimization recommendation) and forecasting (intelligence) components for Threshold, Anomaly and Billing/Forecast alerts.

## Goals
- Threshold Alerts: per-widget/user-defined numeric rules (GT/LT/GTE/LTE/EQ).
- Anomaly Alerts: real-time z-score / deviation detection using precomputed baselines (means/stddevs/percentiles) produced by the existing Optimization Statistics pipeline.
- Forecast Alerts (billing/usage): Use the forecasting service to compare projected spend against explicit user budgets; keep anomaly alerts as a secondary safety net.

---

## Data model

- alerts
  - id uuid
  - user_id uuid
  - widget_id uuid (nullable)
  - alert_type enum: THRESHOLD | ANOMALY | BILLING
  - severity enum: INFO | WARNING | CRITICAL
  - title text
  - message text
  - payload jsonb
  - status enum: ACTIVE | ACKNOWLEDGED | DISMISSED | RESOLVED
  - created_at timestamptz
  - resolved_at timestamptz (nullable)

- budgets
  - budget_id uuid
  - user_id uuid
  - scope enum: TENANT | ACCOUNT | RESOURCE
  - scope_id uuid
  - amount numeric
  - currency text
  - window_days integer
  - enabled boolean
  - created_at timestamptz
  - updated_at timestamptz

Note: Optimization baseline records are already persisted to the tenant `optimization_metric_statistics` table by the Optimization Statistics service.

---

## High-level data flow

1. Metrics and costs are ingested to tenant tables (`normalized_metrics`, `normalized_costs`) and emit metric events via the `notify_metric_event()` trigger.
2. An Evaluation Worker subscribes to metric/cost events and runs detectors.
3. Detector order:
   - Threshold detector (widget thresholds).
   - Anomaly detector (precomputed baselines from `optimization_metric_statistics`).
   - Billing/Forecast detector (budget comparison using forecasts).
4. On trigger, persist an `alerts` record and call the SSE broadcast with `alert` event targeted to the user.
5. Frontend renders toast + inbox item; APIs support list/ack/dismiss.

---

## Implementation: Threshold Detector

- Lookup active thresholds scoped to widget/resource/metric and evaluate operator (GT/LT/GTE/LTE/EQ).
- Use DB index storing last_alerted_at for duplications.
- Persist `alerts` row and broadcast SSE on violation.

SSE payload example (threshold):
{
  "alert_id":"uuid",
  "type":"THRESHOLD",
  "severity":"WARNING",
  "title":"CPU > 80% on i-0123",
  "message":"CPU usage 92% for instance i-0123 (threshold 80%)",
  "timestamp":"2026-09-16T12:34:56Z",
  "widget_id":"uuid",
  "payload": {
    "metric_name":"CPUUtilization",
    "metric_value":92,
    "threshold_operator":"GT",
    "threshold_value":80,
    "period_start":"...",
    "period_end":"..."
  }
}

---

## Implementation: Anomaly Detector

- Reuse `optimization_metric_statistics` (p95, mean, stddev) produced by the Optimization Statistics pipeline.
- Steps:
  1. Query baseline for resource_id + canonical metric name + preferred window.
  2. If `standard_deviation` > 0 compute z = (value - average) / standard_deviation.
  3. Trigger if abs(z) >= thresholds: WARNING=2, CRITICAL=3.
  4. Upgrade existing ACTIVE alerts rather than creating duplicates.
- Persist alert and send SSE.

---

## Implementation: Forecast / Billing Alerts

Design principle: require user-configured budgets as the primary billing alert trigger. Use the forecasting service to compare to budgets. Keep anomaly detection as a secondary safety net for tenants without budgets or if budgets are disabled.

1. Triggering model
   - Budget check against forecasted upper quantile.

2. Forecast source
   - Use Chronos forecasting via the intelligence service to get `forecast_median`, `forecast_q1`, `forecast_q3` for the next `window_days`.

3. Calculation steps
   - Build historical totals:
     - Query tenant `normalized_costs` for the past `window_days` and compute `historical_total` and daily mean/stddev.
   - Call forecasting service with the historical daily series and `forecast_horizon = window_days`.
   - Compute:
     - `projected_median_total = sum(forecast_median)`
     - `projected_q90_total` (use `forecast_q3` as conservative upper quantile; map quantile semantics consistently)
   - Compare to configured budget amount for the same scope (tenant/account/resource).

4. Checks & thresholds 
   - Budget check (primary):
     - WARNING when `projected_q90_total >= budget * 0.9`
     - CRITICAL when `projected_q90_total >= budget`
   - Relative growth check (secondary):
     - WARNING when `projected_median_total >= historical_total * 1.3` (30% growth)
     - CRITICAL when `projected_median_total >= historical_total * 2.0` (100% growth)
   - Optional z-score of projected daily mean vs historical daily mean:
     - WARNING z >= 2, CRITICAL z >= 3

5. Alert creation & payload
   - Persist `alerts` with `alert_type: BILLING` and include `budget_id` in `payload`.
   - Broadcast SSE.

Example SSE payload (billing):
{
  "alert_id":"uuid",
  "type":"BILLING",
  "severity":"WARNING",
  "title":"30-day projected spend may exceed budget",
  "message":"Projected 30d spend X vs budget B",
  "timestamp":"2026-09-16T12:00:00Z",
  "payload":{
    "forecast_median":[...],
    "forecast_q1":[...],
    "forecast_q3":[...],
    "forecast_horizon_days":30,
    "projected_median_total": X,
    "projected_q90_total": Y,
    "budget_id": "uuid",
    "budget_amount": B,
    "historic_total_30d": H
  }
}

---

## APIs & UI

- Budgets:
  - `POST /api/budgets`
  - `GET /api/budgets?scope=...&scopeId=...`
  - `PUT /api/budgets/{id}`
  - `DELETE /api/budgets/{id}`

- Alerts:
  - `GET /api/alerts?status=ACTIVE&page=1&size=20`
  - `POST /api/alerts/{id}/acknowledge`
  - `POST /api/alerts/{id}/dismiss`
  - `GET /api/alerts/{id}`

UI: Add Budget settings in the dashboard (tenant/account/resource scoped), allow enabling/disabling budgets and setting amount/window.

---