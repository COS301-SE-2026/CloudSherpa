DEMO_USER_ID="add_userId_here"

while true
do
  FROM="$(date -u -d '10 minutes ago' +%Y-%m-%dT%H:%M:%SZ)"
  TO="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

  curl -X POST http://localhost:8081/api/events/ingest/mock \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": \"${DEMO_USER_ID}\",
      \"provider\": \"AWS\",
      \"accountId\": \"test-account\",
      \"from\": \"${FROM}\",
      \"to\": \"${TO}\",
      \"includeUsage\": true,
      \"scopes\": [
        {
          \"provider\": \"AWS\",
          \"accountId\": \"test-account\"
        }
      ]
    }"

  sleep 5
done