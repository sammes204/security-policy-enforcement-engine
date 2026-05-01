# Security Policy Enforcement Engine API

## Health

`GET /health`

Returns service health metadata.

## Scan

`POST /scan`

Request:

```json
{
  "input": "Admin panel is public and has no MFA"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "summary": "Public admin access detected.",
    "risks": [
      {
        "name": "Exposed admin panel",
        "severity": "high",
        "description": "The admin panel is reachable from the internet."
      }
    ],
    "recommendations": ["Restrict access and enforce MFA."]
  }
}
```

## Report

`POST /report`

Returns a structured report with `summary`, `risks`, and `recommendations`.

## Ask AI

`POST /ask-ai`

Request:

```json
{
  "question": "How should admin access be protected?"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "answer": "Use least privilege, MFA, and network restrictions.",
    "recommendations": ["Review access logs regularly."]
  }
}
```

## Generate Report

`POST /generate-report`

Compatibility endpoint for existing clients.

Request:

```json
{
  "input": "TLS 1.0 is enabled on the payment API"
}
```

Response:

```json
{
  "title": "TLS finding",
  "summary": "Weak TLS is enabled.",
  "recommendations": ["Disable TLS 1.0", "Require TLS 1.2 or newer"]
}
```

## Recommend

`POST /recommend`

Compatibility endpoint for existing clients.

Request:

```json
{
  "input": "Public admin panel has no IP restrictions"
}
```

Response:

```json
[
  {
    "action_type": "fix",
    "description": "Restrict admin access to trusted networks.",
    "priority": "high"
  }
]
```

All endpoints return JSON errors in this shape:

```json
{
  "success": false,
  "error": {
    "message": "Input is required",
    "code": 400
  }
}
```
