# Automated Alerts

## Overview

This document describes the initial design for the "Automated Alerts" WOW Factor: a real-time alerting engine that notifies users about threshold violations, statistical anomalies, and impending billing overruns. Alerts are delivered in real time using the repo's existing SSE mechanism.

Goals
- Threshold Alerts: let users define simple per-widget limits (e.g., CPU > 80%).
- Anomaly Detection: detect deviations from statistical baselines (moving average + stddev).
- Billing Forecasting: warn when projected spend likely exceeds budgets.
- Real-time delivery via SSE so users receive immediate notifications.

Data flow
1. Metrics are ingested and normalized by the ingestion pipeline.
2. Evaluation worker consumes new metrics.
3. Worker checks thresholds, consults baselines (for anomaly), and consults forecasts (for billing).
4. On trigger, worker persists an `alerts` record and calls SSE broadcast to the owning user.
5. Frontend receives SSE `alert` event and shows toast + adds to Alerts inbox.

Notes: existing SSE server-side endpoints and client usage are present in the repo and can be reused for alert delivery.

## Data model (initial suggestion)

Alerts table
- id (uuid)
- user_id (uuid)
- widget_id (uuid)
- alert_type (enum): THRESHOLD | ANOMALY | BILLING
- severity (enum): INFO | WARNING | CRITICAL
- title (string)
- message (string)
- payload (jsonb)
- status (enum): ACTIVE | ACKNOWLEDGED | DISMISSED | RESOLVED
- created_at (timestamptz)
- resolved_at (timestamptz) — nullable

Widget thresholds table
- id (uuid)
- widget_id (uuid)
- user_id (uuid)
- metric_name (string)
- operator (enum): GT | LT | GTE | LTE | EQ
- value (numeric)
- severity (enum)
- enabled (boolean)
- created_at, updated_at

## SSE / Event schema

Event name: `alert`

Example payload:
{
  "alert_id": "uuid",
  "type": "THRESHOLD" | "ANOMALY" | "BILLING",
  "severity": "WARNING",
  "title": "CPU > 80% on i-0123",
  "message": "CPU usage 92% for instance i-0123 (threshold 80%)",
  "timestamp": "2026-09-16T12:34:56Z",
  "widget_id": "uuid or null",
  "payload": {
    "metric_name": "CPUUtilization",
    "metric_value": 92,
    "period_start": "...",
    "period_end": "..."
  }
}

Backend contract: producers call existing SSE layer with the user's UUID and event name "alert". Payload must be JSON-serializable.

## APIs (initial endpoints)

Thresholds
- POST /api/thresholds
- GET /api/thresholds?widgetId={id}
- PUT /api/thresholds/{id}
- DELETE /api/thresholds/{id}

Alerts
- GET /api/alerts?status=ACTIVE&page=1&size=20
- POST /api/alerts/{id}/acknowledge
- POST /api/alerts/{id}/dismiss
- GET /api/alerts/{id} (details)

## Worker Design - Threshold Evaluation

Source of truth for incoming metrics:
- Subscribe to ingestion stream (near real-time).

Behavior
- For each incoming metric point, list active thresholds applicable (by widget, metric, resource).
- Evaluate operator (GT/LT/GTE/LTE/EQ).
- If violation detected:
  - Create an `alerts` record.
  - Call SSE broadcast to user.
- Record metric value, timestamp, and context in alert payload.

## Anomaly Detection - Baseline & Real-Time Detector

Baseline computation
- For each (resource_id, metric_name) compute mean and stddev over a configurable lookback window (e.g., 7 days).
- Store baseline entries: {key, window_start, window_end, mean, stddev, count, last_updated}.

## Billing Forecast Design

TBD