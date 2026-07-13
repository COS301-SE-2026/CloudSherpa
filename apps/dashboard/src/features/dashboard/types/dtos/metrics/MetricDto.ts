export type MetricDTO = {
  resourceId: string;
  metricName: string;
  metricType: string;
  metricValue: number;
  unit: string | null;
  periodStart: string;
  periodEnd: string;
  sampleCount: number;
};
