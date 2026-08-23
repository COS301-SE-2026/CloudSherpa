import http from 'k6/http';
import { sleep, check } from 'k6';

const LOCAL = true;

const BASE_URL = LOCAL ? 'http://host.docker.internal:8083' : 'http://service:8080'; // NOSONAR within same docker network with no TLS enforcer

const now = new Date().toISOString();
const thirtyDaysBack = new Date(Date.now() - 30*  86_400_000).toISOString();

export const options = {
  vus: 5,
  duration: '30s',
};

const credentials = {
  email: 'nfr-test-user@nfr-test.com',
  password: 'nfr-test-pass@123!'
}

const params = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export function setup() {
    const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify(credentials), params);

    return {
        authToken: res.cookies['auth_token'][0].value
    };
}

export default function (data) { // NOSONAR how k6 expects it
  const jar = http.cookieJar();
  jar.set(`${BASE_URL}`, 'auth_token', data.authToken);

  let res = http.get(`${BASE_URL}/analytics/historical?from=${thirtyDaysBack}&to=${now}&interval=daily`);
  check(res, { "status is 200": (res) => res.status === 200 });
  sleep(1);
}
