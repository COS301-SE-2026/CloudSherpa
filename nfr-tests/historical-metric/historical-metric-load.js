import { loginSetup } from '../common/utils/login-setup.js';
import historicalMetricEndpoint from './historical-metric-utils.js';

const now = new Date().toISOString();
const thirtyDaysBack = new Date(Date.now() - 30*  86_400_000).toISOString();

export const options = {
  stages: [
    { duration: '1m', target: 20 }, 
    { duration: '3m', target: 20 },
    { duration: '1m', target: 0 },
  ],

  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.01']   
  }
};

const timeWindow = {
  from: thirtyDaysBack,
  to: now
}

export function setup() {
    return loginSetup();
}


export default function historicalMetricLoad(data) { 
  historicalMetricEndpoint(data);
}
