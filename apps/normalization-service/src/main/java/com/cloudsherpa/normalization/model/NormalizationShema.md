### Table Fields

`metric_id`
- Uniquely identifies each billing/usage event
- **Why?** : attach tags, debug ingestion, trace anomalies

`provider`
- AWS / Azure / GCP
- **Why?** : multi-cloud, mix providers

`usage_start` `usage_end`
- Represent the actual time window of usage
- **Why?** : time-series graphs, forecasting

`resource_id`
- nullable for some costs that do not have resources
- Identifies the specific resource (VM, bucket, etc.)
- **Why?** : rightsizing, optimization recommendations

`service`
- Answers: “What service is costing us the most?”
- Raw provider service (e.g. AmazonEC2, AmazonS3)
- **Why?** : cost breakdown per service, dashboards, optimization insights

`service_category`
- Normalized category 
- **Why?** : Compare across providers, give meaningful insights

`usage_amount`
- How much was consumed
- **Why?** : forecasting, efficiency metrics, anomaly detection (usage spikes)

`usage_unit`
- Give meaning to the usage
- **Why?** : compare usage

`effective_cost`
- Normalized cost (after discounts, savings plans)
- **Why?** : Show true cost, so no misleading dashboard

`currency`
- Providers may return different currencies
- **Why?** : Unified financial view

`pricing_model`
- Identifies pricing type: on_demand, reserved, savings_plan, spot, dedicated

### Initial ERD
[ERD](https://drive.google.com/file/d/1FTI1yyspUUmKRQ-zIwfUZSFhvivJWjwb/view?usp=sharing)