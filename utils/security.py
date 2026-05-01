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


def is_prompt_injection_attempt(text):
    lowered = text.lower()
    return any(re.search(pattern, lowered) for pattern in PROMPT_INJECTION_PATTERNS)


def is_sql_injection_attempt(text):
    lowered = text.lower()
    return any(re.search(pattern, lowered) for pattern in SQL_INJECTION_PATTERNS)
