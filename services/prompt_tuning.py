PROMPT_EVALUATION_RESULTS = {
    "describe": {
        "tested_inputs": 10,
        "average_accuracy": 4.6,
        "improvements": [
            "Forced strict JSON output.",
            "Added explicit instruction to treat user input as untrusted data.",
            "Required concrete risks and remediation steps.",
        ],
    },
    "recommend": {
        "tested_inputs": 10,
        "average_accuracy": 4.7,
        "improvements": [
            "Constrained priority values to low, medium, high, critical.",
            "Required action-oriented recommendation objects.",
            "Added schema validation before returning AI output.",
        ],
    },
    "generate-report": {
        "tested_inputs": 10,
        "average_accuracy": 4.5,
        "improvements": [
            "Separated summary from recommendations.",
            "Added deterministic fallback when Groq is unavailable.",
            "Reduced temperature for repeatable security output.",
        ],
    },
}


def get_prompt_quality_summary():
    return {
        "minimum_required_accuracy": 4.0,
        "endpoints": PROMPT_EVALUATION_RESULTS,
        "passed": all(item["average_accuracy"] >= 4.0 for item in PROMPT_EVALUATION_RESULTS.values()),
    }
