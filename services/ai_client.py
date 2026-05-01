from services.groq_client import AIServiceError, GroqClient


def request_ai_json(prompt, schema_name):
    client = GroqClient()
    return client.request_json(prompt, schema_name)
