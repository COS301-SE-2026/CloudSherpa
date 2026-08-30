import http from 'k6/http';
import { sleep, check } from 'k6';
import { loginSetup } from './common/utils/login-setup.js';
import { config } from './common/utils/config.js';

const BASE_URL = __ENV.BASE_URL ?? 'http://host.docker.internal:8083';

const now = new Date().toISOString();
const thirtyDaysBack = new Date(Date.now() - 30*  86_400_000).toISOString();

export const options = {
  vus: 5,
  duration: '30s',
};


const queryObjects = [
  {
    resourceId: "10000000-0000-0000-0000-000000000001",
    metricName: "CPUUtilization"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000002",
    metricName: "NetworkIn"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000003",
    metricName: "NetworkOut"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000004",
    metricName: "DiskReadBytes"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000005",
    metricName: "DiskWriteBytes"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000006",
    metricName: "DatabaseConnections"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000007",
    metricName: "ReadLatency"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000008",
    metricName: "Invocations"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000009",
    metricName: "CPUUtilization"
  },
  {
    resourceId: "10000000-0000-0000-0000-000000000010",
    metricName: "CPUUtilization"
  }
]

const timeWindow = {
  from: thirtyDaysBack,
  to: now
}

export function setup() {
    return loginSetup();
}


export default function (data) { // NOSONAR how k6 expects it
  const jar = http.cookieJar();
  jar.set(`${BASE_URL}`, 'auth_token', data.authToken);

  const responses = http.batch(
    queryObjects.map((object) => ['POST', `${BASE_URL}/analytics/downsampled-historical-series`, JSON.stringify({...object, ...timeWindow}), config.basicJsonHeaderParams])
  );

  responses.forEach((response) => {
    check(response, { "status is 200": (response) => response.status === 200 })
  })

  sleep(1);
}
