from utils.security import MAX_INPUT_LENGTH, is_prompt_injection_attempt, sanitize_input

ALLOWED_PRIORITIES = {"low", "medium", "high", "critical"}


def validate_input_payload(payload):
    if not isinstance(payload, dict):
        return "JSON body is required", None

    raw_input = payload.get("input")
    if raw_input is None:
        return "Input is required", None
    if not isinstance(raw_input, str):
        return "Input must be a string", None

    user_input = sanitize_input(raw_input)
    if not user_input:
        return "Input cannot be empty", None
    if len(user_input) > MAX_INPUT_LENGTH:
        return f"Input must be {MAX_INPUT_LENGTH} characters or fewer", None
    if is_prompt_injection_attempt(user_input):
        return "Potential prompt injection detected", None

    return None, user_input


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
