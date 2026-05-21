#!/usr/bin/env bash
set -euo pipefail

INGEST_URL="${INGEST_URL:-http://localhost:8081/api/events/ingest/mockNoise}"
USER_ID="${USER_ID:-11111111-2222-3333-4444-555555555555}"
ACCOUNT_ID="${ACCOUNT_ID:-test-account}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-5}"
PERIOD_SECONDS="${PERIOD_SECONDS:-5}"
BATCH_COUNT="${BATCH_COUNT:-12}"
START_EPOCH="${START_EPOCH:-}"

if (( PERIOD_SECONDS <= 0 )); then
  echo "PERIOD_SECONDS must be > 0" >&2
  exit 1
fi

if (( BATCH_COUNT <= 0 )); then
  echo "BATCH_COUNT must be > 0" >&2
  exit 1
fi

if [[ -z "${START_EPOCH}" ]]; then
  START_EPOCH="$(date -u +%s)"
fi

echo "Posting ${BATCH_COUNT} mock-noise batches to ${INGEST_URL} every ${INTERVAL_SECONDS}s."
echo "Each batch covers new time only, with period ${PERIOD_SECONDS}s."
echo "First datapoint starts at $(date -u -d "@${START_EPOCH}" +"%Y-%m-%dT%H:%M:%SZ")."

current_from_epoch="${START_EPOCH}"

for ((batch = 1; batch <= BATCH_COUNT; batch++)); do
  now_epoch="$(date -u +%s)"

  # Prevent overlapping ingestion
  if (( current_from_epoch > now_epoch )); then
    sleep "$((current_from_epoch - now_epoch))"
    now_epoch="$(date -u +%s)"
  fi

  from_epoch="${current_from_epoch}"
  to_epoch="${now_epoch}"

  if (( to_epoch < from_epoch )); then
    to_epoch="${from_epoch}"
  fi

  from="$(date -u -d "@${from_epoch}" +"%Y-%m-%dT%H:%M:%SZ")"
  to="$(date -u -d "@${to_epoch}" +"%Y-%m-%dT%H:%M:%SZ")"

  curl -fsS -X POST "${INGEST_URL}" \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": \"${USER_ID}\",
      \"from\": \"${from}\",
      \"to\": \"${to}\",
      \"period\": ${PERIOD_SECONDS},
      \"includeUsage\": true,
      \"includeBilling\": false,
      \"scopes\": [
        {
          \"provider\": \"AWS\",
          \"accountId\": \"${ACCOUNT_ID}\",
          \"serviceScopes\": [
            {
              \"name\": \"AWS/EC2\",
              \"instances\": [
                {
                  \"identifierName\": \"InstanceId\",
                  \"values\": [
                    \"i-0ec321a1c8ed4915c\",
                    \"i-0123456789abcdef0\"
                  ]
                }
              ],
              \"metrics\": [
                \"CPUUtilization\",
                \"NetworkIn\",
                \"NetworkOut\",
                \"DiskReadBytes\",
                \"DiskWriteBytes\"
              ]
            },
            {
              \"name\": \"AWS/RDS\",
              \"instances\": [
                {
                  \"identifierName\": \"DBInstanceIdentifier\",
                  \"values\": [
                    \"prod-orders-db\",
                    \"analytics-db\"
                  ]
                }
              ],
              \"metrics\": [
                \"CPUUtilization\",
                \"DatabaseConnections\",
                \"ReadLatency\",
                \"WriteLatency\",
                \"FreeStorageSpace\"
              ]
            },
            {
              \"name\": \"AWS/LAMBDA\",
              \"instances\": [
                {
                  \"identifierName\": \"FunctionName\",
                  \"values\": [
                    \"payment-service\",
                    \"email-processor\"
                  ]
                }
              ],
              \"metrics\": [
                \"Invocations\",
                \"Errors\",
                \"Duration\",
                \"Throttles\"
              ]
            },
            {
              \"name\": \"AWS/DYNAMODB\",
              \"instances\": [
                {
                  \"identifierName\": \"TableName\",
                  \"values\": [
                    \"UsersTable\",
                    \"OrdersTable\"
                  ]
                }
              ],
              \"metrics\": [
                \"ConsumedReadCapacityUnits\",
                \"ConsumedWriteCapacityUnits\",
                \"ReadThrottleEvents\",
                \"WriteThrottleEvents\"
              ]
            },
            {
              \"name\": \"AWS/S3\",
              \"instances\": [
                {
                  \"identifierName\": \"BucketName\",
                  \"values\": [
                    \"cloudsherpa-prod-data\",
                    \"cloudsherpa-logs\"
                  ]
                }
              ],
              \"metrics\": [
                \"BucketSizeBytes\",
                \"NumberOfObjects\",
                \"AllRequests\",
                \"FirstByteLatency\"
              ]
            }
          ]
        }
      ]
    }"

  echo
  echo "Batch ${batch}/${BATCH_COUNT}: ingested mock noise for ${from} -> ${to}"

  current_from_epoch="$((to_epoch + PERIOD_SECONDS))"

  if (( batch < BATCH_COUNT )); then
    sleep "${INTERVAL_SECONDS}"
  fi
done
