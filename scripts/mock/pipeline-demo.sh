DEMO_USER_ID="add_userId_here"

while true
do
  curl -X POST http://localhost:8081/api/events/ingest/mock \
    -H "Content-Type: application/json" \
    -H "tenant-id: ${DEMO_USER_ID}" \
    -d "{
      \"userId\": \"${DEMO_USER_ID}\",
      \"provider\": \"AWS\",
      \"accountId\": \"test-account\",
      \"from": "2026-04-28T00:00:00Z\",
      \"to": "2026-05-05T00:00:00Z\",
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