import pytest

from app import create_app


@pytest.fixture()
def client():
    app = create_app()
    app.config.update(TESTING=True)
    return app.test_client()


def test_scan_returns_structured_report(client, monkeypatch):
    def fake_request_ai_json(_prompt, schema_name):
        assert schema_name == "policy_scan"
        return {
            "summary": "Public admin access detected",
            "risks": [
                {
                    "name": "Exposed admin panel",
                    "severity": "high",
                    "description": "The admin panel is reachable from the internet.",
                }
            ],
            "recommendations": ["Restrict admin access to trusted IP ranges."],
        }

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    response = client.post("/scan", json={"input": "Admin panel is public"})

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["data"]["risks"][0]["severity"] == "high"


def test_report_rejects_sql_injection(client):
    response = client.post("/report", json={"input": "' OR 1=1 --"})

    assert response.status_code == 400
    assert "sql injection" in response.get_json()["error"]["message"].lower()


def test_ask_ai_accepts_question_field(client, monkeypatch):
    def fake_request_ai_json(_prompt, schema_name):
        assert schema_name == "security_answer"
        return {
            "answer": "Use least privilege and network restrictions.",
            "recommendations": ["Add MFA for administrators."],
        }

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    response = client.post("/ask-ai", json={"question": "How should admin access be protected?"})

    assert response.status_code == 200
    assert response.get_json()["data"]["answer"].startswith("Use least privilege")


def test_scan_falls_back_on_invalid_ai_schema(client, monkeypatch):
    def fake_request_ai_json(_prompt, _schema_name):
        return {"summary": "missing risks and recommendations"}

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    response = client.post("/scan", json={"input": "Weak password policy"})

    assert response.status_code == 502
    payload = response.get_json()
    assert payload["is_fallback"] is True
    assert payload["success"] is False


def test_ask_ai_handles_ai_service_error(client, monkeypatch):
    from services.ai_client import AIServiceError

    def fake_request_ai_json(_prompt, _schema_name):
        raise AIServiceError("AI service request failed")

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    response = client.post("/ask-ai", json={"input": "Review my policy"})

    assert response.status_code == 200
    assert response.get_json()["data"]["answer"].startswith("The AI provider is unavailable")


def test_security_headers_are_added(client, monkeypatch):
    def fake_request_ai_json(_prompt, _schema_name):
        return {
            "summary": "No critical risks",
            "risks": [],
            "recommendations": ["Keep monitoring enabled."],
        }

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    response = client.post("/report", json={"input": "Review logging policy"})

    assert response.headers["X-Content-Type-Options"] == "nosniff"
    assert response.headers["X-Frame-Options"] == "DENY"
    assert response.headers["Referrer-Policy"] == "no-referrer"
    assert response.headers["X-Request-ID"]


def test_middleware_strips_html_before_route(client, monkeypatch):
    captured = {}

    def fake_request_ai_json(prompt, _schema_name):
        captured["prompt"] = prompt
        return {
            "summary": "HTML stripped",
            "risks": [],
            "recommendations": ["Continue validation."],
        }

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    response = client.post("/scan", json={"input": "<b>Admin panel</b> is public"})

    assert response.status_code == 200
    assert "<b>" not in captured["prompt"]
    assert "Admin panel is public" in captured["prompt"]


def test_describe_endpoint_returns_description(client, monkeypatch):
    def fake_request_ai_json(_prompt, _schema_name):
        return {
            "summary": "Public admin access should be restricted.",
            "risks": [],
            "recommendations": ["Restrict access."],
        }

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    response = client.post("/describe", json={"input": "Admin panel is public"})

    assert response.status_code == 200
    assert response.get_json()["description"] == "Public admin access should be restricted."


def test_rate_limit_is_30_per_minute(client, monkeypatch):
    def fake_request_ai_json(_prompt, _schema_name):
        return {
            "summary": "ok",
            "risks": [],
            "recommendations": ["ok"],
        }

    monkeypatch.setattr("services.policy_service.request_ai_json", fake_request_ai_json)

    for _ in range(30):
        assert client.post("/scan", json={"input": "normal policy"}).status_code == 200

    response = client.post("/scan", json={"input": "normal policy"})

    assert response.status_code == 429
