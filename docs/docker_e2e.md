# Docker End-to-End Validation

Date: 2026-05-01

## Commands

```powershell
docker compose up --build
```

```powershell
curl http://localhost:5000/health
curl -X POST http://localhost:5000/describe -H "Content-Type: application/json" -d "{\"input\":\"Admin panel is public\"}"
curl -X POST http://localhost:5000/recommend -H "Content-Type: application/json" -d "{\"input\":\"TLS 1.0 is enabled\"}"
curl -X POST http://localhost:5000/generate-report -H "Content-Type: application/json" -d "{\"input\":\"Public S3 bucket\"}"
```

## Expected Result

- Container builds successfully from `Dockerfile`.
- Service listens on port `5000`.
- `/health` returns `status: ok`.
- `/describe`, `/recommend`, and `/generate-report` return JSON.
- If `GROQ_API_KEY` is not provided, deterministic fallback responses are returned without failing the demo.
