# Info

Last updated: 2026/09/16 \
By: Gerard Jordaan \
Revision: 1 \
Feature "launch" (internal deadline): 2026/09/20 

## Feature description

CloudSherpa webhooks expose CloudSherpa *events* to third-party applications by `POST`ing a standardized JSON payload to an endpoint configured by the user.

### Events

Events can be defined as what triggers a webhook payload delivery. The convention followed for events is a full-stop-delimited type, where some examples could be `usage.threshold`, `usage.anomaly`, and `resources.discovery`. The type of an event is important since it is used to identify the webhook delivery payload and its schema.

### Payloads

Full payloads will be used (a term formally defined in the [Standard Webhooks Specification](https://github.com/standard-webhooks/standard-webhooks/blob/main/spec/standard-webhooks.md)) since there is no public-facing CloudSherpa API outside of the webhooks (i.e. making it less useful to send thin payloads containing only a resource ID). All information about the event will be included in the webhook delivery.

The payload is JSON-formatted and follows the basic structure below (taken directly from the [Standard Webhooks Specification](https://github.com/standard-webhooks/standard-webhooks/blob/main/spec/standard-webhooks.md)):

```json
{
  "type": "example.event",
  "timestamp": "2022-11-03T20:26:10.344522Z",
  "data": {
    "foo": "bar",
    "fizzbuzz": 2
  }
}
```

### HTTP Headers

Headers to be attached to the HTTP request are:

#### `webhook-id`

* Uniquely identifies the webhook message
* Remains the same across retries

#### `webhook-timestamp`

* Time at which the webhook delivery attempt was made
* Unix timestamp in seconds

#### `webhook-signature`

* HMAC-SHA256 signature
* The content to be signed is in the format `webhook-id.webhook-timestamp.payload`
* See the security section below

### Failures and retries

Since the delivery endpoints are user-configured, webhook delivery needs to:

* Be resilient against failures
* Communicate failures to the user

Failures do not necessarily mean that the endpoint is misconfigured or that the consumer (in this case, the client application receiving the request) is failing. They could also be caused by network-related issues. A retry mechanism should therefore be put in place to ensure reliable delivery.

A bounded exponential backoff strategy is followed, where the retry schedule is:

```text
Immediate -> 5 seconds -> 5 minutes -> 30 minutes -> 2 hours -> 5 hours -> 10 hours -> 14 hours -> 20 hours -> 24 hours -> stop
```

Functionality allowing the user to manually retry a failed delivery has been deemed useful and will be attempted if time allows.

### Design

Producer-consumer architecture.

A bounded in-memory queue will be used as the message bus, requiring no additional infrastructure. The behaviour when the queue is full still needs to be defined, particularly whether producers should block, reject new messages, or apply another form of backpressure.

### Security

Webhook payloads are to be delivered over an HTTPS connection to ensure message confidentiality. Message authenticity and integrity are ensured using the HMAC-SHA256 signature scheme.

Symmetric signing has been chosen while accepting its security trade-offs compared to asymmetric signing, since webhook payloads do not contain information that, if obtained, would result in a severe compromise.

The content to be signed is in the format `webhook-id.webhook-timestamp.payload`. It is signed using the endpoint secret. A unique secret is generated when a new webhook endpoint is added and is only shown to the user once.

## Relevant links

[Standard followed for the webhook implementation](https://github.com/standard-webhooks/standard-webhooks/blob/main/spec/standard-webhooks.md)
