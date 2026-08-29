import http from 'k6/http';
import { sleep, check } from 'k6';
import { loginSetup } from '../common/utils/login-setup.js';
import { config } from '../common/utils/config.js';

export const options = {
  vus: 1,
  duration: '30s',
};

export function setup() {
    return loginSetup();
}

export default function (data) { // NOSONAR how k6 expects it
  const jar = http.cookieJar();
  jar.set(`${config.baseUrl}`, 'auth_token', data.authToken);

  let res = http.post(`${config.baseUrl}/intelligence/forecasting/billing`, JSON.stringify({
    forecastSteps: 30
  }), config.basicJsonHeaderParams);

  check(res, { "status is 200": (res) => res.status === 200 });
  sleep(1);
}
