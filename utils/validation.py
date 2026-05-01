from utils.security import MAX_INPUT_LENGTH, sanitize_input

ALLOWED_PRIORITIES = {"low", "medium", "high", "critical"}
ALLOWED_SEVERITIES = {"low", "medium", "high", "critical"}


def validate_text_payload(payload, allowed_fields=("input",)):
    if not isinstance(payload, dict):
        return "JSON body is required", None

    raw_input = None
    for field in allowed_fields:
        if field in payload:
            raw_input = payload.get(field)
            break

    if raw_input is None:
        return f"{allowed_fields[0].capitalize()} is required", None
    if not isinstance(raw_input, str):
        return f"{allowed_fields[0].capitalize()} must be a string", None

    user_input = sanitize_input(raw_input)
    if not user_input:
        return f"{allowed_fields[0].capitalize()} cannot be empty", None
    if len(user_input) > MAX_INPUT_LENGTH:
        return f"{allowed_fields[0].capitalize()} must be {MAX_INPUT_LENGTH} characters or fewer", None
    return None, user_input


def validate_input_payload(payload):
    return validate_text_payload(payload)


def validate_policy_report(payload):
    if not isinstance(payload, dict):
        return "Report must be an object"
    if not _is_non_empty_string(payload.get("summary")):
        return "Summary is required"
    risks = payload.get("risks")
    if not isinstance(risks, list):
        return "Risks must be a list"
    for risk in risks:
        if not isinstance(risk, dict):
            return "Each risk must be an object"
        if not _is_non_empty_string(risk.get("name")):
            return "Risk name is required"
        severity = risk.get("severity")
        if not isinstance(severity, str) or severity.lower() not in ALLOWED_SEVERITIES:
            return "Risk severity is invalid"
        if not _is_non_empty_string(risk.get("description")):
            return "Risk description is required"
    recommendations = payload.get("recommendations")
    if not isinstance(recommendations, list) or not all(_is_non_empty_string(item) for item in recommendations):
        return "Recommendations must be a list of strings"
    return None


def validate_ai_answer(payload):
    if not isinstance(payload, dict):
        return "AI answer must be an object"
    if not _is_non_empty_string(payload.get("answer")):
        return "Answer is required"
    recommendations = payload.get("recommendations")
    if recommendations is not None and (
        not isinstance(recommendations, list) or not all(_is_non_empty_string(item) for item in recommendations)
    ):
        return "Recommendations must be a list of strings"
    return None


def validate_report(payload):
    if not isinstance(payload, dict):
        return "Report must be an object"
    if not _is_non_empty_string(payload.get("title")):
        return "Report title is required"
    if not _is_non_empty_string(payload.get("summary")):
        return "Report summary is required"
    recommendations = payload.get("recommendations")
    if not isinstance(recommendations, list) or not recommendations:
        return "Report recommendations must be a non-empty list"
    if not all(_is_non_empty_string(item) for item in recommendations):
        return "Report recommendations must contain only non-empty strings"
    return None


def validate_recommendations(payload):
    if not isinstance(payload, list) or not payload:
        return "Recommendations must be a non-empty list"

    for item in payload:
        if not isinstance(item, dict):
            return "Each recommendation must be an object"
        if not _is_non_empty_string(item.get("action_type")):
            return "Recommendation action_type is required"
        if not _is_non_empty_string(item.get("description")):
            return "Recommendation description is required"
        priority = item.get("priority")
        if not isinstance(priority, str) or priority.lower() not in ALLOWED_PRIORITIES:
            return "Recommendation priority is invalid"
    return None


def _is_non_empty_string(value):
    return isinstance(value, str) and bool(value.strip())
