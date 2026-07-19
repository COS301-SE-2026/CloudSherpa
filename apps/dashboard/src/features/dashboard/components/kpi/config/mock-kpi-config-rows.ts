import type { KPIConfigTableRow } from "@/features/dashboard/components/kpi/config/columns";

type MockKpiResourceTuple = [
    resourceName: string,
    resourceId: string,
    service: string,
    provider: string,
    connection: string,
];

const mockKpiResourceTuples: MockKpiResourceTuple[] = [
    ["Production API Gateway", "api-gw-prod-01", "API Gateway", "AWS", "Production AWS"],
    ["Customer Database", "rds-customer-prod", "RDS", "AWS", "Production AWS"],
    ["Billing Worker Cluster", "eks-billing-workers", "EKS", "AWS", "Production AWS"],
    ["Analytics Storage Bucket", "s3-analytics-events", "S3", "AWS", "Data AWS"],
    ["Cloud Cost Export", "bigquery-cost-export", "BigQuery", "GCP", "Finance GCP"],
    [
        "Production Load Balancer",
        "alb-prod-public",
        "Elastic Load Balancing",
        "AWS",
        "Production AWS",
    ],
    ["Order Processing Queue", "sqs-order-processing", "SQS", "AWS", "Production AWS"],
    ["Invoice Archive", "s3-invoice-archive", "S3", "AWS", "Data AWS"],
    ["Events Warehouse", "redshift-events-prod", "Redshift", "AWS", "Data AWS"],
    ["Realtime Metrics Stream", "kinesis-metrics-stream", "Kinesis", "AWS", "Data AWS"],
    ["Finance Reports Dataset", "bq-finance-reports", "BigQuery", "GCP", "Finance GCP"],
    [
        "Cost Anomaly Function",
        "cloud-function-cost-anomaly",
        "Cloud Functions",
        "GCP",
        "Finance GCP",
    ],
    ["Budget Alerts Topic", "pubsub-budget-alerts", "Pub/Sub", "GCP", "Finance GCP"],
    ["Customer Cache", "elasticache-customer-prod", "ElastiCache", "AWS", "Production AWS"],
    ["Media Processing Jobs", "batch-media-processing", "AWS Batch", "AWS", "Production AWS"],
    ["Audit Log Bucket", "s3-audit-logs", "S3", "AWS", "Data AWS"],
    ["Dataflow Cost Pipeline", "dataflow-cost-pipeline", "Dataflow", "GCP", "Finance GCP"],
    ["Forecast Model Training", "vertex-forecast-training", "Vertex AI", "GCP", "Finance GCP"],
    ["Container Registry", "ecr-cloudsherpa-services", "ECR", "AWS", "Production AWS"],
    ["Daily ETL Orchestrator", "stepfn-daily-etl", "Step Functions", "AWS", "Data AWS"],
];

export const mockKpiConfigRows: KPIConfigTableRow[] = mockKpiResourceTuples.map(
    ([resourceName, resourceId, service, provider, connection]) => ({
        resourceName,
        resourceId,
        service,
        provider,
        connection,
    })
);
