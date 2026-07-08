TENANT_ID="05323fab-3e1b-429b-abcc-0f13d3889747"

while true
do
  curl -X POST http://localhost:8081/api/events/ingest/mock \
    -H "Content-Type: application/json" \
    -H "tenant-id: $TENANT_ID" \
    -d '{
      "from": "2026-07-06T08:00:00Z",
      "to": "2026-07-06T09:00:00Z",
      "scopes": [
        {
          "provider": "AWS",
          "accountId": "123456789012",
          "serviceScopes": [
            {
              "service": "EC2",
              "regions": ["us-east-1"],
              "metrics": [
                {
                  "name": "CPUUtilization",
                  "statistic": "Average"
                }
              ]
            }
          ]
        }
      ]
    }'
  sleep 5
done