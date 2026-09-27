// Observational NFR test: thresholds will be added once an acceptable
// degradation target has been established.

import { loginSetup } from '../common/utils/login-setup.js';
import historicalMetricEndpoint from './historical-metric-utils.js';
import { handleDegradationSummary } from '../common/utils/scalability-utils.js';

export const options = {
    scenarios: {
        baseline: {
            executor: 'constant-vus',
            exec: 'historicalMetricScalability',
            vus: 5,
            duration: '1m'
        },
        load: {
            executor: 'constant-vus',
            exec: 'historicalMetricScalability',
            vus: 50,
            duration: '5m',
            startTime: '1m'
        }
    },
    thresholds: {
        'http_req_duration{scenario:baseline}': [],
        'http_req_duration{scenario:load}': [],
    },
};

export function setup() {
    return loginSetup();
}

export function historicalMetricScalability(data) { 
  return historicalMetricEndpoint(data);
}

export function handleSummary(data) {
    return handleDegradationSummary(data, 'http_req_duration{scenario:baseline}', 'http_req_duration{scenario:load}');
}