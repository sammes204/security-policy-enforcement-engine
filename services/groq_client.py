from services.ai_client import AIServiceError, _build_payload, _provider_config

import logging

import requests

logger = logging.getLogger(__name__)
REQUEST_TIMEOUT_SECONDS = 30


def ask_ai(prompt):
    provider, url, api_key = _provider_config()
    if not api_key:
        raise AIServiceError("AI provider is not configured")

    payload = _build_payload(prompt)
    logger.info("legacy_ai_request provider=%s prompt_length=%s", provider, len(prompt))

    try:
        response = requests.post(
            url,
            headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
            json=payload,
            timeout=REQUEST_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        logger.info("legacy_ai_response provider=%s", provider)
        return response.json()
    except requests.RequestException as exc:
        logger.exception("legacy_ai_request_failed provider=%s", provider)
        raise AIServiceError("AI service request failed") from exc
    except ValueError as exc:
        logger.exception("legacy_ai_response_invalid provider=%s", provider)
        raise AIServiceError("AI service returned invalid response") from exc
