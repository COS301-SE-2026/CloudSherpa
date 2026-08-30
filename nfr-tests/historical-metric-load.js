import http from 'k6/http';
import { sleep, check } from 'k6';
import { loginSetup } from './common/utils/login-setup.js';

const BASE_URL = __ENV.BASE_URL ?? 'http://host.docker.internal:8083';

const now = new Date().toISOString();
const thirtyDaysBack = new Date(Date.now() - 30*  86_400_000).toISOString();

export const options = {
  vus: 5,
  duration: '30s',
};

export function setup() {
    return loginSetup();
}

export default function (data) { // NOSONAR how k6 expects it
  const jar = http.cookieJar();
  jar.set(`${BASE_URL}`, 'auth_token', data.authToken);

  let res = http.get(`${BASE_URL}/analytics/historical?from=${thirtyDaysBack}&to=${now}&interval=daily`);
  check(res, { "status is 200": (res) => res.status === 200 });
  sleep(1);
}
