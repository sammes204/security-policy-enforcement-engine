import logging

from flask import Blueprint, jsonify, request

from services.report_service import generate_security_report, get_recommendations
from utils.responses import error_response
from utils.validation import validate_input_payload

logger = logging.getLogger(__name__)
report_bp = Blueprint("report", __name__)


@report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    validation_error, user_input = validate_input_payload(request.get_json(silent=True))
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = generate_security_report(user_input)
    if service_error:
        logger.warning("Report generation failed: %s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify(result), 200


@report_bp.route("/recommend", methods=["POST"])
def recommend():
    validation_error, user_input = validate_input_payload(request.get_json(silent=True))
    if validation_error:
        return error_response(validation_error, 400)

    result, service_error = get_recommendations(user_input)
    if service_error:
        logger.warning("Recommendation generation failed: %s", service_error)
        return error_response(service_error, 502, is_fallback=True)

    return jsonify(result), 200
