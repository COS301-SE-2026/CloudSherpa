# CloudSherpa Ingestion Service

## Billing & Usage Data Model Documentation

---

## 1. Overview

This document defines the **canonical data models** used by the ingestion service to represent:

- **Billing data** (financial cost records)
- **Usage data** (resource/metric consumption)

These models are designed to normalize data across:

- Amazon Web Services (AWS)
- Microsoft Azure
- Google Cloud Platform (GCP)

and pass through the ingestion and normalization pipeline for downstream service use.

---

## 2. Design Principles

### 2.1 Provider-Agnostic

All fields are normalized to support multiple cloud providers without embedding provider-specific schemas.

### 2.2 Scoping

Every record includes identifiers for:

- AWS accounts
- Azure subscriptions
- GCP projects

This ensures correct attribution when multiple deployments are involved.

### 2.3 Timestamps

All timestamps use `java.time.Instant` (UTC) to:

- Avoid timezone ambiguity
- Ensure consistency across distributed systems

### 2.4 Pipeline Records

Records passed through the ingestion pipeline must be:

- Self-contained
- Traceable
- Idempotent-friendly (identical calls provide identical results when the data for those calls have been finalized)

---

## 3. BillingRecord Model

### 3.1 Purpose

Represents **cost data** associated with cloud resource usage.

This is the **source of truth for cost analysis**. Cost prediction will
use `UsageRecord` data as a temporary proxy, but for detailed analysis, periodic billing
information may be ingested to serve as a more accurate reference. The challenge with
this is that some providers only update these records periodically.

---

### 3.2 Structure

| Field      | Type   | Description                            |
| ---------- | ------ | -------------------------------------- |
| `recordId` | UUID   | Unique identifier for the record       |
| `provider` | String | Cloud provider (`AWS`, `AZURE`, `GCP`) |

---

### Scope Fields

The diverse interfaces across the three primary providers necessitates
multiple scope fields to allow the request for data to be provider-agnostic for
the consumer.

| Field              | Description                   |
| ------------------ | ----------------------------- |
| `accountId`        | AWS account identifier        |
| `subscriptionId`   | Azure subscription identifier |
| `projectId`        | GCP project identifier        |
| `billingAccountId` | Billing account (Azure/GCP)   |

---

### Resource Information

| Field          | Description                             |
| -------------- | --------------------------------------- |
| `serviceName`  | Cloud service (e.g. EC2, VM, BigQuery)  |
| `resourceId`   | Unique resource identifier              |
| `resourceType` | Type of resource (instance, disk, etc.) |
| `region`       | Geographic region                       |

---

### Cost Information

| Field           | Description                        |
| --------------- | ---------------------------------- |
| `cost`          | Monetary cost                      |
| `usageQuantity` | Quantity consumed (e.g. hours, GB) |
| `unit`          | Unit of measurement                |
| `currency`      | Currency code (e.g. USD)           |
| `pricingModel`  | Pricing type (Reserved, Spot etc.) |

---

### Time Fields

| Field                | Description           |
| -------------------- | --------------------- |
| `usageStartTime`     | Start of usage window |
| `usageEndTime`       | End of usage window   |
| `billingPeriodStart` | Billing cycle start   |
| `billingPeriodEnd`   | Billing cycle end     |

---

### Metadata

| Field  | Description               |
| ------ | ------------------------- |
| `tags` | Provider-defined metadata |

---

### Pipeline Metadata

| Field                | Description                         |
| -------------------- | ----------------------------------- |
| `ingestionTimestamp` | When the record was ingested        |
| `ingestionId`        | Identifier for ingestion job/event  |
| `source`             | Data source (e.g. CUR, API, Export) |

---

## 4. UsageRecord Model

### 4.1 Purpose

Represents **operational usage metrics**.

Examples:

- CPU utilization
- Network throughput
- Disk I/O

For some APIs such as CloudWatch, one API request may only fetch up
to a certain amount of metrics in the free tier, so multiple requests may need to be made to retrieve
all relevant usage data.

---

### 4.2 Structure

| Field      | Type   | Description       |
| ---------- | ------ | ----------------- |
| `recordId` | UUID   | Unique identifier |
| `provider` | String | Cloud provider    |

---

### Scope Fields

| Field            | Description        |
| ---------------- | ------------------ |
| `accountId`      | AWS account        |
| `subscriptionId` | Azure subscription |
| `projectId`      | GCP project        |

---

### Resource Information

| Field          | Description         |
| -------------- | ------------------- |
| `resourceId`   | Resource identifier |
| `resourceType` | Type of resource    |
| `serviceName`  | Service name        |
| `region`       | Region              |

---

### Usage Metrics

| Field        | Description                   |
| ------------ | ----------------------------- |
| `metricName` | Metric (e.g. CPU utilization) |
| `value`      | Metric value                  |
| `unit`       | Metric unit                   |

---

### Time Fields

| Field         | Description                 |
| ------------- | --------------------------- |
| `timestamp`   | Metric timestamp            |
| `periodStart` | Start of aggregation window |
| `periodEnd`   | End of aggregation window   |

---

### Dimensions

| Field        | Description                         |
| ------------ | ----------------------------------- |
| `dimensions` | Provider-specific metric dimensions |

Examples:

- AWS: `InstanceId`
- Azure: `resourceId`
- GCP: `labels`

---

### Metadata

| Field  | Description       |
| ------ | ----------------- |
| `tags` | Resource metadata |

---

### Pipeline Metadata

| Field                | Description                            |
| -------------------- | -------------------------------------- |
| `ingestionTimestamp` | When record was ingested               |
| `ingestionId`        | Trace identifier                       |
| `source`             | Data source (e.g. CloudWatch, Monitor) |

---

## 5. Important Notes

### 5.1 Billing vs Usage Mismatch

Billing and usage data:

- Are collected from different systems
- May have different aggregation levels
- Will not perfectly align

As previously stated, usage may be used as temporary predictor for price, and
normalisation will be required.

---

### 5.2 Multi-Scope Ingestion

Each record represents **one scope**:

- One AWS account
- One Azure subscription
- One GCP project

Connectors will loop through their known scopes to provide all data
for a particular account, allowing data collection across deployments, projects, etc.

---

## 6. Summary

These models provide:

- A unified abstraction over multi-cloud billing and usage
- Strong support for event-driven ingestion
- A scalable foundation for analytics and optimization

---
