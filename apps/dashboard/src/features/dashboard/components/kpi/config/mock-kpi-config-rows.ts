import type { KPIConfigTableRow } from "@/features/dashboard/components/kpi/config/columns";

type MockKpiResourceTuple = [
    resourceName: string | null,
    chargeId: string,
    resourceId: string,
    service: string,
    provider: string,
    chargeCost: number,
];

const mockKpiResourceTuples: MockKpiResourceTuple[] = [
    ["Production API Gateway", "api-gw-prod-01", "API Gateway", "AWS", "Production AWS", 8],
    ["Customer Database", "rds-customer-prod", "RDS", "AWS", "Production AWS", 8],
    ["Billing Worker Cluster", "eks-billing-workers", "EKS", "AWS", "Production AWS", 8],
    ["Analytics Storage Bucket", "s3-analytics-events", "S3", "AWS", "Data AWS", 8],
    ["Cloud Cost Export", "bigquery-cost-export", "BigQuery", "GCP", "Finance GCP", 8],
    [
        "Production Load Balancer",
        "alb-prod-public",
        "Elastic Load Balancing",
        "AWS",
        "Production AWS",

        9,
    ],
    ["Order Processing Queue", "sqs-order-processing", "SQS", "AWS", "Production AWS", 8],
    ["Invoice Archive", "s3-invoice-archive", "S3", "AWS", "Data AWS", 8],
    ["Events Warehouse", "redshift-events-prod", "Redshift", "AWS", "Data AWS", 8],
    ["Realtime Metrics Stream", "kinesis-metrics-stream", "Kinesis", "AWS", "Data AWS", 8],
    ["Finance Reports Dataset", "bq-finance-reports", "BigQuery", "GCP", "Finance GCP", 8],
    [
        "Cost Anomaly Function",
        "cloud-function-cost-anomaly",
        "Cloud Functions",
        "GCP",
        "Finance GCP",

        8,
    ],
    ["Budget Alerts Topic", "pubsub-budget-alerts", "Pub/Sub", "GCP", "Finance GCP", 8],
    ["Customer Cache", "elasticache-customer-prod", "ElastiCache", "AWS", "Production AWS", 8],
    ["Media Processing Jobs", "batch-media-processing", "AWS Batch", "AWS", "Production AWS", 8],
    ["Audit Log Bucket", "s3-audit-logs", "S3", "AWS", "Data AWS", 8],
    ["Dataflow Cost Pipeline", "dataflow-cost-pipeline", "Dataflow", "GCP", "Finance GCP", 8],
    ["Forecast Model Training", "vertex-forecast-training", "Vertex AI", "GCP", "Finance GCP", 8],
    ["Container Registry", "ecr-cloudsherpa-services", "ECR", "AWS", "Production AWS", 8],
    ["Daily ETL Orchestrator", "stepfn-daily-etl", "Step Functions", "AWS", "Data AWS", 8],
];

export const mockKpiConfigRows: KPIConfigTableRow[] = mockKpiResourceTuples.map(
    ([resourceName, chargeId, resourceId, service, provider, chargeCost]) => ({
        chargeId: chargeId,
        resourceId: resourceId,
        service: service,
        provider: provider,
        resourceName: resourceName,
        chargeCost: chargeCost,
    })
);
