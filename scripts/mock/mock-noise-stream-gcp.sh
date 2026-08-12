#!/usr/bin/env bash
set -euo pipefail

INGEST_URL="${INGEST_URL:-http://localhost:8081/api/events/ingest/mockNoise}"
USER_ID="${USER_ID:-5ebe4340-c5ec-4833-ad93-06abf4609f03}"
PROJECT_ID="${PROJECT_ID:-my-gcp-project-000}"
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
          \"provider\": \"GCP\",
          \"projectId\": \"${PROJECT_ID}\",
          \"serviceScopes\": [
            {
              \"name\": \"gce_instance\",
              \"instances\": [
                {
                  \"identifierName\": \"instance_id\",
                  \"instances\": [
                    {\"identifier\": \"instance-1\"},
                    {\"identifier\": \"instance-2\"}
                  ]
                }
              ],
              \"metrics\": [
                {\"name\": \"compute.googleapis.com/instance/cpu/utilization\"},
                {\"name\": \"compute.googleapis.com/instance/network/received_bytes_count\"},
                {\"name\": \"compute.googleapis.com/instance/network/sent_bytes_count\"},
                {\"name\": \"compute.googleapis.com/instance/disk/read_bytes_count\"},
                {\"name\": \"compute.googleapis.com/instance/disk/write_bytes_count\"}
              ]
            },
            {
              \"name\": \"cloudsql_database\",
              \"instances\": [
                {
                  \"identifierName\": \"instance_id\",
                  \"instances\": [
                    {\"identifier\": \"cloudsql-prod\"},
                    {\"identifier\": \"cloudsql-analytics\"}
                  ]
                }
              ],
              \"metrics\": [
                {\"name\": \"cloudsql.googleapis.com/database/cpu/utilization\"},
                {\"name\": \"cloudsql.googleapis.com/database/disk/bytes_used\"},
                {\"name\": \"cloudsql.googleapis.com/database/network/received_bytes_count\"}
              ]
            },
            {
              \"name\": \"gcs_bucket\",
              \"instances\": [
                {
                  \"identifierName\": \"bucket_name\",
                  \"instances\": [
                    {\"identifier\": \"cloudsherpa-prod-data\"},
                    {\"identifier\": \"cloudsherpa-logs\"}
                  ]
                }
              ],
              \"metrics\": [
                {\"name\": \"storage.googleapis.com/storage/total_bytes\"},
                {\"name\": \"storage.googleapis.com/storage/object_count\"},
                {\"name\": \"storage.googleapis.com/network/received_bytes_count\"}
              ]
            },
            {
              \"name\": \"pubsub_subscription\",
              \"instances\": [
                {
                  \"identifierName\": \"subscription_id\",
                  \"instances\": [
                    {\"identifier\": \"orders-sub\"},
                    {\"identifier\": \"events-sub\"}
                  ]
                }
              ],
              \"metrics\": [
                {\"name\": \"pubsub.googleapis.com/subscription/num_undelivered_messages\"},
                {\"name\": \"pubsub.googleapis.com/subscription/ack_message_count\"}
              ]
            },
            {
              \"name\": \"pubsub_topic\",
              \"instances\": [
                {
                  \"identifierName\": \"topic_id\",
                  \"instances\": [
                    {\"identifier\": \"orders-topic\"},
                    {\"identifier\": \"events-topic\"}
                  ]
                }
              ],
              \"metrics\": [
                {\"name\": \"pubsub.googleapis.com/topic/send_message_operation_count\"}
              ]
            },
            {
              \"name\": \"cloud_run_revision\",
              \"instances\": [
                {
                  \"identifierName\": \"revision_name\",
                  \"instances\": [
                    {\"identifier\": \"payment-service-0001\"},
                    {\"identifier\": \"email-processor-0001\"}
                  ]
                }
              ],
              \"metrics\": [
                {\"name\": \"run.googleapis.com/request_count\"},
                {\"name\": \"run.googleapis.com/container/cpu/utilizations\"},
                {\"name\": \"run.googleapis.com/container/memory/used_bytes\"}
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