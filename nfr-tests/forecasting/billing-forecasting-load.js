import http from 'k6/http';
import { sleep, check } from 'k6';
import { loginSetup } from '../common/utils/login-setup.js';
import { config } from '../common/utils/config.js';

export const options = {
  stages: [
    { duration: '1m', target: 20 }, 
    { duration: '3m', target: 20 },
    { duration: '1m', target: 0 },
  ],

  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01']   
  }
};

export function setup() {

    const loginSetupRes = loginSetup();

    const params = {
        ...config.basicJsonHeaderParams,
        cookies: {
            auth_token: loginSetupRes.authToken
        }
    }

    // Initial request to warm up cache (purpose of test is to test response time after cache warmed up)
    // This is blocking so no need for async handling
    const warmUpRes = http.post(`${config.baseUrl}/intelligence/forecasting/billing`, JSON.stringify({
        forecastSteps: 30
    }), params);

    check(warmUpRes, {"status is 200": (res) => res.status === 200 });

    // Log as example response
    console.log(warmUpRes.body);

    return loginSetupRes;
}

export default function (data) { // NOSONAR how k6 expects it
  const jar = http.cookieJar();
  jar.set(`${config.baseUrl}`, 'auth_token', data.authToken);

  let res = http.post(`${config.baseUrl}/intelligence/forecasting/billing`, JSON.stringify({
    forecastSteps: 30
  }), config.basicJsonHeaderParams);

  check(res, { "status is 200": (res) => res.status === 200 });

  // Check body to ensure request was indeed succesful
  const body = JSON.parse(res.body);
  check(body, {
    "billing forecast value is not undefined": (body) => body.cumalativeBillingForecastValue !== undefined,
    "cumalative past value is not undefined": (body) => body.cumalitivePastForecastingValue !== undefined
  })
  sleep(1);
}
