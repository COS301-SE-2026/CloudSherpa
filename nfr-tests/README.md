## Running Grafana K6 Scripts using docker

```sh
docker run --rm \
  --user "$(id -u):$(id -g)" \
  --volume "$(pwd):/apps" \
  --workdir /apps \
  --interactive \
  --add-host host.docker.internal:host-gateway \
  --env K6_WEB_DASHBOARD=true \
  --env K6_WEB_DASHBOARD_PORT=-1 \
  --env K6_WEB_DASHBOARD_EXPORT=/apps/report.html \
  grafana/k6 run /apps/historical-metric-load.js
```

## NFR Test User

- User ID: `a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246`
- Email: `nfr-test-user@nfr-test.com`
- Username: `nfr-test-user`
- Password: `nfr-test-pass@123!`
