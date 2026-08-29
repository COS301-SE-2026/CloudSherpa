import http from 'k6/http';
import { sleep, check } from 'k6';
import { loginSetup } from '../common/utils/login-setup.js';

const BASE_URL = __ENV.BASE_URL ?? 'http://host.docker.internal:8083';

export const options = {
  vus: 1,
  duration: '30s',
};

export function setup() {
    return loginSetup();
}


const params = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export default function (data) { // NOSONAR how k6 expects it
  const jar = http.cookieJar();
  jar.set(`${BASE_URL}`, 'auth_token', data.authToken);

  let res = http.post(`${BASE_URL}/intelligence/forecasting/billing`, JSON.stringify({
    forecastSteps: 30
  }), params);

  check(res, { "status is 200": (res) => res.status === 200 });
  sleep(1);
}
