import html
import re

MAX_INPUT_LENGTH = 2000

PROMPT_INJECTION_PATTERNS = [
    r"\bignore\s+(all\s+)?previous\b",
    r"\bsystem\s+prompt\b",
    r"\bdeveloper\s+message\b",
    r"\bbypass\b",
    r"\bjailbreak\b",
    r"\bpretend\s+to\b",
    r"\breveal\s+(your\s+)?instructions\b",
    r"\bact\s+as\b",
    r"\bforget\s+(all\s+)?instructions\b",
]

SQL_INJECTION_PATTERNS = [
    r"('|--|;|/\*|\*/)",
    r"\bunion\s+select\b",
    r"\bdrop\s+table\b",
    r"\binsert\s+into\b",
    r"\bdelete\s+from\b",
    r"\bor\s+1\s*=\s*1\b",
]


def sanitize_input(value):
    if value is None:
        return ""
    text = str(value)
    text = re.sub(r"<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>", "", text, flags=re.IGNORECASE)
    text = re.sub(r"<[^>]*>", "", text)
    text = html.escape(text, quote=True)
    text = re.sub(r"[\x00-\x1f\x7f]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def sanitize_json_payload(payload):
    if isinstance(payload, dict):
        return {key: sanitize_json_payload(value) for key, value in payload.items()}
    if isinstance(payload, list):
        return [sanitize_json_payload(value) for value in payload]
    if isinstance(payload, str):
        return sanitize_input(payload)
    return payload


def find_malicious_input(payload):
    for value in _walk_strings(payload):
        if is_prompt_injection_attempt(value):
            return "Potential prompt injection detected"
        if is_sql_injection_attempt(value):
            return "Potential SQL injection detected"
    return None


def is_prompt_injection_attempt(text):
    lowered = text.lower()
    return any(re.search(pattern, lowered) for pattern in PROMPT_INJECTION_PATTERNS)


def is_sql_injection_attempt(text):
    lowered = text.lower()
    return any(re.search(pattern, lowered) for pattern in SQL_INJECTION_PATTERNS)


def _walk_strings(value):
    if isinstance(value, str):
        yield value
    elif isinstance(value, dict):
        for item in value.values():
            yield from _walk_strings(item)
    elif isinstance(value, list):
        for item in value:
            yield from _walk_strings(item)
