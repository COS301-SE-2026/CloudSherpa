# Agentic Dashboard Construction

## Info

**Last updated:** 2026/09/18

**By:**  Cherise Heyl

**Revision:** 1

**Feature "launch" (internal deadline):** 2026/09/28

---

## Feature description

CloudSherpa Agentic Dashboard Construction allows users to create and iteratively refine dashboards using natural-language instructions.

Users interact with an AI assistant through a multi-message session. The AI can inspect CloudSherpa resources, accounts, metrics, and dashboard capabilities through controlled read-only tools and construct a dashboard configuration based on that information.

AI-generated dashboards are never written directly to the user's active dashboard.

Instead, every generated configuration is persisted as an immutable version in dedicated AI staging tables for a user. Each subsequent refinement produces a new version. Users can inspect previous versions, return to an earlier version, or explicitly apply a selected version to their CloudSherpa dashboard.

The AI therefore has two categories of capabilities:

- **Read-only tools** for discovering valid CloudSherpa resources, metrics, and dashboard capabilities. 
- **One write tool** for saving an AI-generated dashboard configuration to the staging/version store. 

The AI has no tool capable of modifying, deleting, or applying a user's real dashboard.

---

# Goals

-  Allow users to construct dashboards using natural-language instructions. 
-  Allow multi-turn refinement of generated dashboards. 
-  Ensure AI-generated configurations reference valid CloudSherpa resources and metrics. 
-  Provide the AI with sufficient CloudSherpa metadata through controlled read-only tools. 
-  Persist every generated dashboard as an immutable version. 
-  Allow users to select and apply any generated version. 
-  Allow users to effectively roll back by applying an earlier version. 
-  Keep AI-generated state separate from production dashboard state. 
-  Reuse existing CloudSherpa dashboard, resource, metric, authentication, and rendering infrastructure. 
-  Ensure AI cannot directly modify production dashboards or perform cloud infrastructure operations. 

---
# AI capabilities

The AI will interact with CloudSherpa through a small set of controlled tools.

## Read-only tools

The AI requires CloudSherpa metadata to construct valid dashboard configurations.

### `get_cloud_accounts`

Returns the cloud accounts accessible to the authenticated user.

Example:

```json
{
  "accounts": [
    {
      "id": "uuid",
      "provider": "AWS",
      "displayName": "Production AWS"
    },
    {
      "id": "uuid",
      "provider": "GCP",
      "displayName": "Production GCP"
    }
  ]
}
```

---

### `get_resources`

Returns resources accessible to the user, optionally filtered by account, provider, or service.

Example:

```json
{
  "resources": [
    {
      "id": "uuid",
      "name": "production-api",
      "provider": "AWS",
      "accountId": "uuid",
      "resourceType": "EC2",
      "region": "af-south-1"
    }
  ]
}
```

The tool must only return resources the authenticated user is authorized to access, and this capability will be reinforced by our current schema-per-tenant architecture.

---

### `get_metrics`

Returns all of the metrics available for a particular resource.

Example:

```json
{
  "resourceId": "uuid",
  "metrics": [
    {
      "id": "cpu_utilization",
      "name": "CPU Utilization",
      "unit": "Percent",
      "supportedChartTypes": [
        "line_chart",
        "gauge_chart"
      ]
    },
    {
      "id": "network_in",
      "name": "Network In",
      "unit": "Bytes",
      "supportedChartTypes": [
        "line_chart"
      ]
    }
  ]
}
```

This prevents the AI from inventing metric names or selecting metrics that are not available for a resource, as well as guiding the AI to the correct chart type. Only percentage units can be applied to a guage chart, for instance.

---

### `get_dashboard_capabilities`

Returns the dashboard and widget capabilities supported by the current CloudSherpa frontend/backend implementation.

Example:

```json
{
  "widgetTypes": [
    {
      "type": "chart",
      "chartTypes": [
        "line_chart",
        "gauge_chart"
      ]
    },
    {
      "type": "kpi"
    }
  ]
}
```

This allows the AI to construct configurations using only supported widget types and chart types.

---

# Write capability

The AI has one write operation:

### `save_dashboard_version`

The tool accepts a validated dashboard configuration and stores it as a new AI dashboard version.

The operation:

1.  Verifies the AI session belongs to the authenticated user. 
2.  Validates the dashboard configuration. 
3.  Validates resource and metric references. 
4.  Creates a new immutable version. 
5.  Stores the version in the AI staging tables. 
6.  Updates the session's current version. 
7.  Returns the created version to be displayed to the user. 

The tool does not modify the real dashboard tables as that action is reserved solely for the user.

---

# Dashboard configuration

The AI will produce a structured CloudSherpa dashboard specification rather than arbitrary UI code.

A simplified example:

```json
{
  "title": "Production API Monitoring",
  "timeRange": {
    "from": "now-24h",
    "to": "now"
  },
  "widgets": [
    {
      "type": "chart",
      "displayName": "CPU Utilization",
      "chartType": "line_chart",
      "provider": "AWS",
      "accountId": "uuid",
      "resourceId": "uuid",
      "metricType": "EC2",
      "metricName": "CPUUtilization",
      "startX": 0,
      "startY": 0,
      "width": 6,
      "height": 4
    }
  ]
}
```

The AI does not generate database IDs for widgets. IDs are generated by CloudSherpa.

---

# Data model

AI-generated dashboard state is stored separately from the production dashboard state.

## `ai_session`

```postgresql
id uuid
user_id uuid
created_at timestamptz
last_activity timestamptz
current_version_id uuid nullable
```

A session represents one conversation in the dashboard-construction workflow.

---

## `ai_message`

```postgresql
id uuid
session_id uuid
content text
created_at timestamptz
```

Messages provide the conversation history required for multi-turn dashboard refinement.

For example:

```
User:
Create a dashboard for my production API.

Assistant:
Created version 1.

User:
Add memory usage.

Assistant:
Created version 2.
```

---

## `ai_dashboard_version`

```postgresql
dashboard_id uuid
session_id uuid
version_number integer
parent_version_id uuid nullable
title text
description text nullable
created_at timestamptz
time_from timestamptz,
time_to timestamptz,
predefined_time public.predefined_time_enum,
current boolean DEFAULT false
```

Each version is immutable as previously discussed and the relationship between versions is also stored. This version tree could potentially be displayed to the user to aid in finding the correct refinement along with the description and title. This is not currently planned for implementation but leaves the door open for extending the visualization of the version history in that way.

For example:

```
V1
 │
 └── V2
      │
      └── V3
```

If the user asks to return to V1 and makes another change:

```
       ┌── V2 ── V3
V1 ────┤
       └── V4
```

`parent_version_id` therefore preserves the relationship between refinements without requiring a complex version-control system.

---

## `ai_dashboard_widget`

Mirrors the existing dashboard widget structure.

```postgresql
id uuid
dashboard_version_id uuid
widget_type enum
start_x integer
start_y integer
width integer
height integer
display_name text
```

---

## `ai_chart_widget`

```postgresql
widget_id uuid
chart_type text
chart_colour text nullable
provider text
account_id uuid
resource_id uuid
metric_type text
metric_name text
```

---

## `ai_kpi_widget`

```postgresql
kpi_id uuid
widget_id uuid
aggregation integer
```

The exact types should follow the existing dashboard persistence model.

---

# Applying a dashboard

Applying a version is a normal authenticated CloudSherpa operation and is not an AI tool.

```
POST /ai/sessions/{sessionId}/versions/{versionId}/apply
```

The backend:

1.  Authenticates the user. 
2.  Verifies session ownership. 
3.  Verifies version ownership. 
4.  Loads the AI generated dashboard version. 
5.  Validates the version again. 
6.  Creates the real dashboard and widgets. 
7.  Commits the operation transactionally. 

If any part fails, the transaction is rolled back.

---

# Session termination

When a user terminates an AI dashboard-construction session the following operation will be performed:

```
DELETE /ai/sessions/{sessionId}
```

the associated AI staging data is removed.

This can be implemented using database cascading:

```
ai_session
    │
    ├── ai_message
    │
    └── ai_dashboard_version
            │
            └── ai_dashboard_widget
                    ├── ai_chart_widget
                    └── ai_kpi_widget
```

---

# High-level data flow

1.  User creates an AI dashboard session. 
2.  User sends a natural-language dashboard request. 
3.  AI receives the conversation and available CloudSherpa tools. 
4.  AI calls read-only tools to discover: 
   -  accounts 
   -  resources 
   -  metrics 
   -  dashboard capabilities 
5.  AI constructs a structured dashboard configuration. 
6.  AI calls `save_dashboard_version`. 
7.  Backend validates the configuration. 
8.  A new immutable AI dashboard version is persisted. 
9.  Frontend displays the generated dashboard preview. 
10.  User can request further modifications. 
11.  Each modification creates another version. 
12.  User can select any version. 
13.  User explicitly applies the selected version. 
14.  Backend transactionally creates/updates the real dashboard. 

---

# Security

Security is based on capability separation.

The AI receives only the capabilities required to construct a dashboard, and only the resource information it needs.

## Read access

AI tools execute within the authenticated user's authorization context.

The AI cannot access arbitrary user data because the backend verifies ownership/access before returning the resource.

Likewise, metric queries must be scoped to resources available to the current user. Individual metric values will not be queryable by the AI agent although possibly some ranking method might be employed to ensure that the AI can make a dashboard for "biggest cost drivers" for instance, without knowing what the values are for those

---

## Write access

The only AI write capability is `save_dashboard_version`

This operation can only write to AI staging tables.

It cannot:
-  modify `dashboard` 
-  modify `dashboard_widget` 
-  delete dashboards 
-  modify cloud resources 
-  modify cloud credentials 

---

## Production mutation

Production dashboard mutation requires a separate authenticated application endpoint:

```
User
 │
 ▼
Frontend
 │
 ▼
Authenticated Service
 │
 ▼
Apply selected AI version
 │
 ▼
Real Dashboard
```

The AI cannot invoke this endpoint as a tool. This is reserved as a user endpoint.

---
# Validation

AI output must be validated before being persisted.

Validation should include:

### Dashboard
-  title length 
-  description length 
-  supported time range 

### Widgets
-  supported widget type 
-  valid width/height 
-  display-name length 

### Chart widgets
-  supported chart type 
-  valid provider 
-  valid account 
-  valid resource 
-  valid metric 
-  metric belongs to resource 
-  metric is supported by the selected chart type 

### KPI widgets
-  valid charge IDs 
-  valid aggregation window 

Most importantly, identifiers supplied by the AI must be checked against the resources and metrics accessible to the authenticated user.

---
# APIs & UI

## AI sessions

```
POST /ai/sessions
```

Creates an AI dashboard-construction session.

```
DELETE /ai/sessions/{sessionId}
```

Terminates the session and removes staging data.

---

## Dashboard planning

```
POST /ai/dashboard/plan
```

Request:

```json
{
  "sessionId": "uuid",
  "message": "Create a dashboard showing CPU and memory for my production EC2 instances"
}
```

Response:

```json
{
  "sessionId": "uuid",
  "versionId": "uuid",
  "version": 1,
  "assistantMessage": "I've created a dashboard with CPU and memory monitoring.",
  "dashboard": {
    "...": "..."
  }
}
```

This endpoint is the application-facing entry point for an AI planning request. Internally, the AI can use the CloudSherpa tools described above.

---

## Version history

```
GET /ai/sessions/{sessionId}/versions
```

Returns the versions created during the session.

```
GET /ai/sessions/{sessionId}/versions/{versionId}
```

Returns a specific version.

---
## Apply

```
POST /ai/sessions/{sessionId}/versions/{versionId}/apply
```

Explicitly applies the selected AI-generated version to a real CloudSherpa dashboard.

This endpoint is not exposed as an AI tool.

---
# UI

The dashboard construction interface consists of:

### AI conversation

Users can send natural-language requests such as:

```
Create a dashboard for my production AWS resources.
```

Then:

```
Add CPU and memory usage.
```

Then:

```
Make CPU a gauge and keep memory as a line chart.
```

### Dashboard preview

The generated configuration is rendered using the existing CloudSherpa dashboard components.

The AI does not generate the raw UI chart components.

The existing rendering framework remains responsible for rendering as for normal dashboard creation. This is in an attempt to keep the look and feel of the dashboard similar to user created dashboards, as opposed to allowing the AI to create arbitrary chart elements, which may violate the dashboard restrictions set in place by our dashboard system and result in invalid dashboards. The tradeoff is that we are restricting the chart availability of the dashboard that the AI can create to only explicitly supported chart types.

### Version history

The user can see:

```
Version 4
Version 3
Version 2
Version 1
```

and preview/select a version. Likely each version will have a title and description to make selecting the correct version easier for the user.

### Apply

The user explicitly chooses:

```
Apply version
```

to transfer the selected staging configuration into the real dashboard.

---

# MCP

MCP can be used as the tool interface between the AI model and CloudSherpa.

The initial CloudSherpa MCP surface should remain intentionally small:

```
CloudSherpa MCP Tools

READ
├── get_cloud_accounts
├── get_resources
├── get_metrics
└── get_dashboard_capabilities

WRITE
└── save_dashboard_version
```

The MCP layer should delegate to existing CloudSherpa services rather than directly accessing repositories.

```
AI Model
   │
   ▼
MCP
   │
   ▼
CloudSherpa AI Tool Service
   │
   ├── Existing account/resource services
   ├── Existing metric services
   └── AiDashboardVersionService
```

This provides a clean capability boundary without requiring a large MCP platform for the MVP.

---
# A2UI and Flint

Neither A2UI nor Flint is required for the initial implementation.

The existing CloudSherpa frontend already provides:

-  dashboard layout 
-  GridStack positioning 
-  widget rendering 
-  ECharts rendering 
-  dashboard preview 

Therefore the AI should produce CloudSherpa dashboard configuration, not arbitrary frontend code.

A2UI could be considered later for richer AI-generated interaction components.

Flint could potentially be introduced later as a semantic chart specification/compiler layer if CloudSherpa expands beyond the existing chart types.

Neither is required for the initial two-week implementation.

---

