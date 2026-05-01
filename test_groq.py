import os

from services.groq_client import AIServiceError, GroqClient, build_payload_for_tests


def main():
    if not os.getenv("GROQ_API_KEY"):
        payload = build_payload_for_tests("Return JSON only.")
        print({
            "success": True,
            "mode": "offline",
            "message": "Groq client import and payload construction succeeded; set GROQ_API_KEY for live API test.",
            "model": payload["model"],
        })
        return

    client = GroqClient()
    try:
        response = client.request(
            "Return JSON only: {\"status\":\"ok\",\"message\":\"Groq integration smoke test\"}",
            "smoke_test",
        )
        print(response)
    except AIServiceError as exc:
        print({"success": False, "error": str(exc)})
        raise


if __name__ == "__main__":
    main()
