import pytest

from services.ai_client import AIServiceError, request_ai_json


def test_request_ai_json_extracts_json(monkeypatch):
    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {
                "choices": [
                    {
                        "message": {
                            "content": '```json\n{"answer":"ok","recommendations":["test"]}\n```'
                        }
                    }
                ]
            }

    monkeypatch.setenv("GROQ_API_KEY", "test-key")
    monkeypatch.setattr("services.ai_client.requests.post", lambda *args, **kwargs: FakeResponse())

    result = request_ai_json("prompt", "security_answer")

    assert result == {"answer": "ok", "recommendations": ["test"]}


def test_request_ai_json_rejects_invalid_json(monkeypatch):
    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {"choices": [{"message": {"content": "not-json"}}]}

    monkeypatch.setenv("GROQ_API_KEY", "test-key")
    monkeypatch.setattr("services.ai_client.requests.post", lambda *args, **kwargs: FakeResponse())

    with pytest.raises(AIServiceError, match="invalid JSON"):
        request_ai_json("prompt", "security_answer")
