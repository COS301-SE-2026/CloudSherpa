## Running Grafana K6 Scripts using docker

```sh
docker run --rm \
  --user "$(id -u):$(id -g)" \
  --volume "$(pwd):/apps" \
  --interactive \
  --add-host host.docker.internal:host-gateway \
  --env K6_WEB_DASHBOARD=true \
  --env K6_WEB_DASHBOARD_PORT=-1 \
  --env K6_WEB_DASHBOARD_EXPORT=/apps/report.html \
  --env USER_EMAIL="" \
  --env USER_PASSWORD="" \
  grafana/k6 run - < historical-metric-load.js
```
