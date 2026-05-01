# Security Policy Enforcement Engine

Production-grade AI-backed security report and recommendation service.

## Run locally

```powershell
pip install -r requirements.txt
python app.py
```

## Test

```powershell
pytest
```

## Environment

Create `.env` with:

```text
GROQ_API_KEY=your_api_key
GROQ_MODEL=llama-3.3-70b-versatile
AI_PROVIDER=groq
```

API documentation is available in `docs/api.md`.

## Docker

```powershell
docker compose up --build
```

The service runs on `http://localhost:5000`.

## Security and quality evidence

- `SECURITY.md`
- `docs/final_security_checklist.md`
- `docs/owasp_zap_report.md`
- `docs/prompt_quality_evaluation.md`
- `docs/docker_e2e.md`

## API

- `POST /describe`
- `POST /scan`
- `POST /report`
- `POST /ask-ai`
- `POST /generate-report`
- `POST /recommend`
