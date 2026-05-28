export type MetricDTO = {
  metricId: string;
  accountId: string;
  currency: string | null;
  resourceId: string;
  metricType: string;
  metricName: string;
  metricValue: number;
  periodStart: string;
  periodEnd: string;
  recordedAt: string;
  unit: string;
};
