from flask import jsonify


def error_response(message, status_code, is_fallback=False):
    payload = {
        "success": False,
        "error": {
            "message": message,
            "code": status_code,
        },
    }
    if is_fallback:
        payload["is_fallback"] = True

    return jsonify(payload), status_code
