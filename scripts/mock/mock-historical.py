#!.venv/bin/python3

from datetime import datetime, timedelta
import requests
import sys
import math

args = sys.argv

if len(args) != 4:
    print(    
        "Incorrect number of arguments specified\n" + 
        "Usage: python3 mock-historical.py <from> <to> <interval>"
    )
    exit(1)

try:
    fromDate = datetime.fromisoformat(args[1])
    toDate = datetime.fromisoformat(args[2])
    interval = int(args[3])
except ValueError as e:
    print(f"Invalid date: {e}")
    exit(1)
except IndexError as e:
    print(f"Incorrect number of arguments specified: {e}")
except Exception as e:
    print(f"Error has occured: {e}")

batches = math.floor((toDate - fromDate).total_seconds() / interval)
print(f"Total batches to generate: {batches}" )

url = "http://localhost:8081/api/events/ingest/mockNoise"

for i in range(1, batches + 1):
    print(f"\rCreating batch {i}/{batches} for time {fromDate}", end="", flush=True)
    
    payload = {
        "userId": "11111111-2222-3333-4444-555555555555",
        "from": fromDate.isoformat().replace("+00:00", "Z"),
        "to": fromDate.isoformat().replace("+00:00", "Z"),
        "period": interval,
        "includeUsage": True,
        "includeBilling": False,
        "scopes": [
            {
                "provider": "AWS",
                "accountId": "test-account",
                "serviceScopes": [
                    {
                        "name": "AWS/EC2",
                        "instances": [
                            {
                                "identifierName": "InstanceId",
                                "values": [
                                    "i-0ec321a1c8ed4915c",
                                ],
                            }
                        ],
                        "metrics": [
                            "CPUUtilization",
                            "NetworkIn",
                            "DiskReadBytes",
                        ],
                    },
                    {
                        "name": "AWS/RDS",
                        "instances": [
                            {
                                "identifierName": "DBInstanceIdentifier",
                                "values": [
                                    "prod-orders-db",
                                ],
                            }
                        ],
                        "metrics": [
                            "CPUUtilization",
                            "DatabaseConnections",
                            "ReadLatency",
                            "FreeStorageSpace",
                        ],
                    },
                    {
                        "name": "AWS/LAMBDA",
                        "instances": [
                            {
                                "identifierName": "FunctionName",
                                "values": [
                                    "payment-service",
                                ],
                            }
                        ],
                        "metrics": [
                            "Invocations",
                            "Errors",
                            "Duration",
                            "Throttles",
                        ],
                    },
                    {
                        "name": "AWS/DYNAMODB",
                        "instances": [
                            {
                                "identifierName": "TableName",
                                "values": [
                                    "UsersTable",
                                ],
                            }
                        ],
                        "metrics": [
                            "ConsumedReadCapacityUnits",
                            "ConsumedWriteCapacityUnits",
                            "ReadThrottleEvents",
                        ],
                    },
                    {
                        "name": "AWS/S3",
                        "instances": [
                            {
                                "identifierName": "BucketName",
                                "values": [
                                    "cloudsherpa-prod-data",
                                ],
                            }
                        ],
                        "metrics": [
                            "BucketSizeBytes",
                            "NumberOfObjects",
                            "AllRequests",
                            "FirstByteLatency",
                        ],
                    },
                ],
            }
        ],
    }
    
    response  = requests.post(url, json=payload, timeout=30)
    response.raise_for_status()
    
    fromDate = fromDate + timedelta(seconds=interval)
