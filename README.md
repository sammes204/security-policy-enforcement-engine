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

## API

- `POST /scan`
- `POST /report`
- `POST /ask-ai`
- `POST /generate-report`
- `POST /recommend`
