import { config } from "../common/utils/config.js";
import { loginSetup } from "../common/utils/login-setup.js";
import http from 'k6/http';
import { sleep, check } from "k6";

export const options = {
    scenarios: {
        baseline: {
            executor: 'constant-vus',
            exec: 'usageForecastingScalability',
            vus: 5,
            duration: '1m'
        },
        load: {
            executor: 'constant-vus',
            exec: 'usageForecastingScalability',
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

const payload = JSON.stringify({
    resourceId: "10000000-0000-0000-0000-000000000001",
    metricType: "CPU Utilization",
    forecastHorizon: "2026-10-03T00:00:00Z"
})

export function setup() {
    return loginSetup();
}

export function usageForecastingScalability(data) { 
  const jar = http.cookieJar();
  jar.set(`${config.baseUrl}`, 'auth_token', data.authToken);

    const response = http.post(`${config.baseUrl}/intelligence/forecasting/resource`, payload, config.basicJsonHeaderParams);

    check(response, { 
      "status is 200": (response) => response.status === 200,
      "datapoints returned": (response) => Object.keys(JSON.parse(response.body)).length > 0
    })

  sleep(1);
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