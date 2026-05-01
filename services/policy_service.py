import logging

from services.ai_client import AIServiceError, request_ai_json
from utils.validation import validate_ai_answer, validate_policy_report

logger = logging.getLogger(__name__)


def scan_policy(user_input):
    prompt = f"""
Scan this security policy, finding, or system description:
{user_input}

Return only this JSON object:
{{
  "summary": "short scan summary",
  "risks": [
    {{
      "name": "risk name",
      "severity": "low|medium|high|critical",
      "description": "risk description"
    }}
  ],
  "recommendations": ["clear remediation step"]
}}
"""
    return _request_validated_json(prompt, "policy_scan", validate_policy_report, _fallback_policy_report(user_input))


def generate_report(user_input):
    prompt = f"""
Generate a structured security report from this input:
{user_input}

Return only this JSON object:
{{
  "summary": "executive summary",
  "risks": [
    {{
      "name": "risk name",
      "severity": "low|medium|high|critical",
      "description": "risk description"
    }}
  ],
  "recommendations": ["clear remediation step"]
}}
"""
    return _request_validated_json(prompt, "security_report", validate_policy_report, _fallback_policy_report(user_input))


def ask_security_ai(user_input):
    prompt = f"""
Answer this security policy question:
{user_input}

Return only this JSON object:
{{
  "answer": "concise answer",
  "recommendations": ["optional next step"]
}}
"""
    return _request_validated_json(prompt, "security_answer", validate_ai_answer, _fallback_ai_answer(user_input))


def _request_validated_json(prompt, schema_name, validator, fallback_payload):
    try:
        payload = request_ai_json(prompt, schema_name)
        validation_error = validator(payload)
        if validation_error:
            logger.warning("ai_schema_invalid schema=%s error=%s", schema_name, validation_error)
            return None, "AI returned an unexpected JSON schema"
        return payload, None
    except AIServiceError as exc:
        logger.warning("ai_fallback_used schema=%s error=%s", schema_name, exc)
        return fallback_payload, None


def _fallback_policy_report(user_input):
    return {
        "summary": f"Automated security review completed for: {user_input[:120]}",
        "risks": [
            {
                "name": "Manual validation required",
                "severity": "medium",
                "description": "The AI provider was unavailable, so this deterministic fallback flags the item for review.",
            }
        ],
        "recommendations": [
            "Validate the policy against least privilege, authentication, logging, and network exposure controls.",
            "Repeat the AI review when the Groq provider is available.",
        ],
    }


def _fallback_ai_answer(user_input):
    return {
        "answer": (
            "The AI provider is unavailable. Apply least privilege, enforce authentication, validate inputs, "
            "enable audit logging, and review exposed services for this request."
        ),
        "recommendations": [
            "Perform a manual security review before approving the policy.",
            f"Track the unavailable AI review for follow-up: {user_input[:80]}",
        ],
    }
