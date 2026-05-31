# SherpaDB ERD and Architecture Overview

This document describes the database architecture used for CloudSherpa. The system uses a **Hybrid Multi-Tenant Architecture** built on PostgreSQL and TimescaleDB:
1. **Global Schema (`public`):** The shared space for all users. It holds global data like user accounts, UI preferences, and dashboard layouts.
2. **Tenant Schemas (`tenant_<uuid>`):** A private, isolated database space created uniquely for each customer. This holds their massive lists of cloud resources and time-series metrics without mixing them up with other customers.

---

## Enums (Shared Across All Schemas)

| Enum Name | Allowed Values | Description |
| :--- | :--- | :--- |
| **provider_enum** | `AWS`, `AZURE`, `GCP` | Which cloud provider is being tracked. |
| **status_enum** | `active`, `disabled` | A simple switch to turn features (like data syncing) on or off. |
| **credential_type_enum**| `access_key`, `oauth` | How the system logs into the cloud provider. |
| **account_type_enum** | `aws_account`, `azure_subscription`, `gcp_project` | The specific name the cloud provider uses for an "account." |
| **metric_type_enum** | `cost`, `usage`, `performance` | Broad categories used to group the metric data. |
| **theme_enum** | `light`, `dark` | The visual theme the user prefers for the app. |
| **currency_enum** | `USD`, `EUR`, `ZAR` | The money format used to display cost metrics. |
| **language_enum** | `en`, `es`, `fr` | The language the app is translated into. |
| **ingestion_period_enum**| `1m`, `5m`, `1h` | How often our system fetches new data from the cloud. |
| **predefined_time_enum** | `last_1h`, `last_24h`, `last_7d` | Quick-select time filters for viewing dashboards. |
| **type_enum** | `line_chart`, `guage_chart` | The specific type of visual chart used in a widget. |

---

## Global Infrastructure (Public Schema)

These tables are centralized. They handle core system data, user access, and how the app looks.

### users
The people who log into the application.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **user_id** | UUID | Primary Key | The unique ID for the user. |
| **email** | VARCHAR(320) | Unique, Not Null | The user's email address used to log in. |
| **username** | VARCHAR(100) | Not Null | The name displayed on the screen (e.g., "John Doe"). |
| **password_hash** | VARCHAR(255) | Not Null | The securely scrambled version of the user's password. |
| **created_at** | TIMESTAMPTZ | Default NOW() | The exact date and time the user registered. |

### preferences
Custom visual settings chosen by the user.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **user_id** | UUID | Primary Key, FK | Links to `public.users.user_id`. |
| **theme** | theme_enum | Nullable | Dark mode or light mode choice. |
| **background** | TEXT | Nullable | A web link (URL) to a custom background image. |
| **currency** | currency_enum | Nullable | Their preferred money display (e.g., converting all costs to ZAR). |
| **language** | language_enum | Nullable | Their preferred text language (e.g., Spanish). |
| **sidebar_toggle** | BOOLEAN | Default `true` | True if the user left the side menu open; False if they collapsed it. |

### cloud_connection
The main link connecting a user to their cloud provider (like a bridge to AWS or Azure).

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **connection_id** | UUID | Primary Key | The unique ID for this connection setup. |
| **user_id** | UUID | Foreign Key | The user who created this connection. |
| **provider** | provider_enum| Not Null | The specific cloud provider (e.g., AWS). |
| **status** | status_enum | Default `active` | Set to 'disabled' to temporarily pause data syncing. |
| **created_at** | TIMESTAMPTZ | Default NOW() | When this connection was first created. |

### cloud_credential
Securely stores the passwords, API keys, or tokens needed to safely talk to the cloud provider.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **credential_id** | UUID | Primary Key | The unique ID for these credentials. |
| **connection_id** | UUID | Foreign Key | Links to the specific `cloud_connection`. |
| **provider** | provider_enum| Not Null | Repeated here to make searching for AWS vs GCP keys faster. |
| **credential_type**| credential_type_enum | Not Null | Tells the system how to read the key (e.g., as an `access_key`). |
| **credential_value**| TEXT | Not Null | The heavily encrypted JSON text containing the actual secret keys. |
| **created_at** | TIMESTAMPTZ | Default NOW() | When these credentials were saved. |

#### Credential Payload Format
`credential_value` keeps complex secret keys grouped together in one encrypted block. Before encryption, it looks like this:

```json
{
  "access_key_id": "AKIAIOSFODNN7EXAMPLE",
  "secret_access_key": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
  "region": "AF-SOUTH-1"
}
```

### cloud_account
Represents a specific sub-account inside the cloud provider (like an AWS Account or GCP Project) where resources actually live.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **account_id** | UUID | Primary Key | The unique ID for this cloud account. |
| **connection_id** | UUID | Foreign Key | Links back to the main connection bridge. |
| **account_type** | account_type_enum | Not Null | e.g., 'aws_account' vs 'azure_subscription'. |
| **ingestion_period**| ingestion_period_enum | Nullable | How often we pull data for this account (e.g., every 5m). |
| **display_name** | VARCHAR(255) | Nullable | A custom name the user gave this account (e.g., "Production AWS"). |
| **created_at** | TIMESTAMPTZ | Default NOW() | When we first synced this account. |

### dashboard
A user's custom page used for viewing groups of charts and metrics.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **dashboard_id** | UUID | Primary Key | The unique ID for this dashboard page. |
| **user_id** | UUID | Foreign Key | The user who owns this dashboard. |
| **time_from** | TIMESTAMPTZ | Nullable | A fixed, specific start date for the charts (e.g., Jan 1, 2026). |
| **time_to** | TIMESTAMPTZ | Nullable | A fixed, specific end date for the charts. |
| **predefined_time** | predefined_time_enum | Nullable | A rolling time filter (e.g., "Always show the last 7 days"). |

### widget
A single chart, graph, or number box that sits on a dashboard.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **widget_id** | UUID | Primary Key | The unique ID for this specific chart. |
| **dashboard_id** | UUID | Foreign Key | The dashboard this chart lives on. |
| **type** | type_enum | Not Null | What kind of chart it is (e.g., `line_chart`). |
| **start_x** | INTEGER | Not Null | Where the chart sits horizontally on the screen grid (e.g., column 2). |
| **start_y** | INTEGER | Not Null | Where the chart sits vertically on the screen grid (e.g., row 1). |
| **width** | INTEGER | Not Null | How many columns wide the chart is. |
| **height** | INTEGER | Not Null | How many rows tall the chart is. |
| **display_name** | VARCHAR(100) | Nullable | The title shown above the chart (e.g., "Database CPU Usage"). |

### widget_resource
Connects a specific chart (widget) to the specific cloud resources it should display.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **widget_resource_id**| UUID | Primary Key | Unique ID for this connection. |
| **widget_id** | UUID | Foreign Key | Links to the specific widget chart. |
| **resource_id** | UUID | Cross-Schema | The ID of the cloud resource to track (lives in the tenant schema). |
| **metric_type** | metric_type_enum | Not Null | What kind of data the chart should ask for (e.g., `performance`). |

---

## Tenant Isolation Template (Schema: `tenant_<uuid>`)

These tables hold the massive amounts of data generated by cloud resources. A fresh copy of these tables is created inside a private schema for every single tenant.

### resource
A list of every single cloud asset (servers, databases, hard drives) we found in the tenant's cloud account.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **resource_id** | UUID | Primary Key | Our unique ID for this specific server or bucket. |
| **account_id** | UUID | Cross-Schema FK | Links back up to the `cloud_account` in the public schema. |
| **resource_type** | VARCHAR(255) | Not Null | What the asset actually is (e.g., `AWS::EC2::Instance`). |
| **resource_name** | VARCHAR(255) | Not Null | The standardized display name for the asset, resolved by the backend. |
| **status** | status_enum | Nullable | Is the server currently running (`active`) or stopped (`disabled`)? |
| **tags** | JSONB | GIN Index | User-defined cloud labels saved as a flexible JSON object. |
| **last_updated** | TIMESTAMPTZ | Default NOW() | The last time our system checked on this resource. |
| **created_at** | TIMESTAMPTZ | Default NOW() | When our system first discovered this resource. |

#### Resource Naming Logic
Cloud providers are inconsistent with how they name assets. To prevent the frontend from running complex conditional logic just to display a chart title, the backend resolves a universal `resource_name` before saving it to the database.

The backend resolves the name using this priority fallback:
1. **Dedicated Name Field:** If the cloud provider provides a specific name property.
2. **Tag Search:** If no dedicated field exists, it searches the `JSONB` tags for a key matching `"Name"`.
3. **ID Fallback:** If neither exists, it defaults to using the `resource_id` (e.g., `i-0abcd1234efgh5678`).

#### Schema Performance Tuning (JSONB Tags)
Users add wild, unpredictable tags to their cloud resources. Instead of trying to fit them into strict rows and columns, we save them exactly as they are in a `JSONB` column. 

* **Flexibility:** We can save any tag combination (e.g., `{"team": "frontend", "project": "beta"}`) without breaking the database.
* **Speed:** We use a special **GIN Index**, which allows the database to instantly search inside the JSON object to find specific tags without scanning the whole table.

```sql
CREATE INDEX ix_tenant_resource_tags ON tenant_1234.resource USING GIN (tags);
```

### normalized_metrics
The giant, fast-moving table that holds every single metric data point (CPU spikes, network traffic, billing costs) for every resource.

| Column Name | Data Type | Key/Constraint | Description |
| :--- | :--- | :--- | :--- |
| **resource_id** | UUID | Foreign Key | The specific resource this data point belongs to. |
| **recorded_at** | TIMESTAMPTZ | Not Null | The exact moment our system received and saved this data. |
| **metric_type** | metric_type_enum | Not Null | Is this a `cost`, `usage`, or `performance` metric? |
| **metric_name** | VARCHAR(255) | Not Null | The standardized name of the metric (e.g., `CPUUtilization`). |
| **metric_value**| NUMERIC | Not Null | The actual number measured (e.g., `85.5`). We use NUMERIC to prevent math rounding errors, which is critical for calculating money. |
| **unit** | VARCHAR(50) | Nullable | What the number represents (e.g., `Percent`, `Gigabytes`, `USD`). |
| **currency** | VARCHAR(10) | Nullable | Filled in only if the metric is money-related (e.g., `USD`). |
| **period_start**| TIMESTAMPTZ | Hypertable Key | **Partition Column.** The start time of the measurement window (e.g., 12:00 PM). Frontend charts use this to sort data. |
| **period_end** | TIMESTAMPTZ | Not Null | The end time of the measurement window (e.g., 12:05 PM). |

---

## Advanced Database Architecture Features

### 1. TimescaleDB Partitioning & Pruning Strategy
Because the `normalized_metrics` table will easily hit millions or billions of rows, standard PostgreSQL would slow to a crawl. We use **TimescaleDB** to automate it.

* **Automatic Slicing:** TimescaleDB secretly slices the massive metrics table into smaller, daily or weekly "chunks" based on the `period_start` time. 
* **Fast Querying (Pruning):** If a user asks for "yesterday's CPU usage," the database doesn't look through all the data; it completely ignores last month's chunks and only opens yesterday's chunk.
* **Instant Chart Sorting:** We created a specific index so that when the frontend asks for data to draw a line chart, the database hands it back already perfectly sorted by time:

```sql
CREATE INDEX ix_tenant_resource_metric_time 
ON tenant_1234.normalized_metrics (resource_id, metric_name, period_start DESC);
```

### 2. Real-Time Event Broadcasting
When new metrics hit the database, we want the live frontend dashboards to update instantly without needing to constantly ask the database "Are there any updates?"

* **The Trigger:** We set a rule on the metrics table that acts immediately after new data is saved.
* **The Broadcast:** The database packages the new row of data into a JSON string and essentially acts like a loudspeaker, broadcasting the data out on a channel called `metric_events`. Any backend server listening to that channel gets the data instantly and pushes it to the user's browser.

#### Broadcast Payload Example
```json
{
  "resource_id": "c1d2e3f4-a5b6-c7d8-e9f0-1234567890cd",
  "recorded_at": "2026-05-18T13:26:24.000Z",
  "metric_type": "performance",
  "metric_name": "CPUUtilization",
  "metric_value": 85.5,
  "unit": "Percent",
  "currency": null,
  "period_start": "2026-05-18T13:20:00.000Z",
  "period_end": "2026-05-18T13:25:00.000Z"
}
```