import json
import logging
import re

logger = logging.getLogger(__name__)


def extract_json(text):
    if not isinstance(text, str) or not text.strip():
        return None

    cleaned = re.sub(r"```(?:json)?|```", "", text, flags=re.IGNORECASE).strip()
    decoder = json.JSONDecoder()

    for index, char in enumerate(cleaned):
        if char not in "{[":
            continue
        try:
            parsed, _ = decoder.raw_decode(cleaned[index:])
            return parsed
        except json.JSONDecodeError:
            continue

    logger.warning("Unable to extract JSON from AI response length=%s", len(text))
    return None
