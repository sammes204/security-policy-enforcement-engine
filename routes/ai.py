import logging

from flask import Blueprint, jsonify, request

from services.policy_service import ask_security_ai, generate_report, scan_policy
from services.report_service import generate_security_report, get_recommendations
from utils.responses import error_response
from utils.validation import validate_text_payload

logger = logging.getLogger(__name__)
ai_bp = Blueprint("ai", __name__)


def _payload():
    return getattr(request, "sanitized_json", None) or request.get_json(silent=True)


@ai_bp.route("/scan", methods=["POST"])
def scan():
    validation_error, user_input = validate_text_payload(_payload())
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = scan_policy(user_input)
    if service_error:
        logger.warning("scan_failed error=%s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify({"success": True, "data": result}), 200


@ai_bp.route("/report", methods=["POST"])
def report():
    validation_error, user_input = validate_text_payload(_payload())
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = generate_report(user_input)
    if service_error:
        logger.warning("report_failed error=%s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify({"success": True, "data": result}), 200


@ai_bp.route("/ask-ai", methods=["POST"])
def ask_ai():
    validation_error, user_input = validate_text_payload(
        _payload(),
        allowed_fields=("question", "input"),
    )
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = ask_security_ai(user_input)
    if service_error:
        logger.warning("ask_ai_failed error=%s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify({"success": True, "data": result}), 200


@ai_bp.route("/generate-report", methods=["POST"])
def legacy_generate_report():
    validation_error, user_input = validate_text_payload(_payload())
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = generate_security_report(user_input)
    if service_error:
        logger.warning("legacy_report_failed error=%s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify(result), 200


@ai_bp.route("/recommend", methods=["POST"])
def legacy_recommend():
    validation_error, user_input = validate_text_payload(_payload())
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = get_recommendations(user_input)
    if service_error:
        logger.warning("legacy_recommend_failed error=%s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify(result), 200


@ai_bp.route("/describe", methods=["POST"])
def describe():
    validation_error, user_input = validate_text_payload(_payload())
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = scan_policy(user_input)
    if service_error:
        logger.warning("describe_failed error=%s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify({
        "description": result["summary"],
        "risks": result["risks"],
        "recommendations": result["recommendations"],
    }), 200
