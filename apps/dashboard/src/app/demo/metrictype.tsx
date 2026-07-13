export type MetricData = {
  metric_id: string;
  recorded_at: string; 
  environment_id: string;
  resource_id: string;
  service_category: string;
  usage_amount: number;
  usage_unit: string;
  cost_amount: number;
  currency: string;
};