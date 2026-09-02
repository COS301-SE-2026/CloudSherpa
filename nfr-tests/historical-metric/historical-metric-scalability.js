import { loginSetup } from '../common/utils/login-setup.js';
import historicalMetricEndpoint from './historical-metric-utils.js';

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
            vus: 100,
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
    console.log(JSON.stringify(data));
    const baseline = data.metrics['http_req_duration{scenario:baseline}'];
    const load = data.metrics['http_req_duration{scenario:load}'];

    const baselineP95 = baseline.values['p(95)'];
    const loadP95 = load.values['p(95)'];

    const degradation = ((loadP95 - baselineP95) / baselineP95) * 100;

    console.log(`Baseline p95: ${baselineP95} ms`);
    console.log(`Load p95:     ${loadP95} ms`);
    console.log(`Degradation:  ${degradation.toFixed(2)}%`);

    return {};
}