import logging
import time
from uuid import uuid4

from flask import Flask, jsonify, request
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

from routes.ai import ai_bp
from utils.security import find_malicious_input, sanitize_json_payload

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s %(message)s",
)

logger = logging.getLogger(__name__)


def create_app():
    app = Flask(__name__)
    app.config["JSON_SORT_KEYS"] = False

    Limiter(
        get_remote_address,
        app=app,
        default_limits=["30 per minute"],
        storage_uri="memory://",
    )

    app.register_blueprint(ai_bp)

    @app.before_request
    def sanitize_and_log_request():
        request.request_id = str(uuid4())
        request.start_time = time.perf_counter()
        if request.method in {"POST", "PUT", "PATCH"} and request.is_json:
            payload = request.get_json(silent=True)
            if payload is not None:
                sanitized_payload = sanitize_json_payload(payload)
                security_error = find_malicious_input(sanitized_payload)
                if security_error:
                    logger.warning(
                        "malicious_input_rejected path=%s request_id=%s reason=%s",
                        request.path,
                        request.request_id,
                        security_error,
                    )
                    return jsonify({
                        "success": False,
                        "error": {"message": security_error, "code": 400},
                    }), 400
                request.sanitized_json = sanitized_payload
        logger.info(
            "api_request method=%s path=%s request_id=%s remote_addr=%s",
            request.method,
            request.path,
            request.request_id,
            request.remote_addr,
        )

    @app.after_request
    def finalize_response(response):
        start_time = getattr(request, "start_time", time.perf_counter())
        request_id = getattr(request, "request_id", str(uuid4()))
        duration_ms = int((time.perf_counter() - start_time) * 1000)
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["Referrer-Policy"] = "no-referrer"
        response.headers["Content-Security-Policy"] = "default-src 'none'; frame-ancestors 'none'"
        response.headers["X-Request-ID"] = request_id
        logger.info(
            "api_response method=%s path=%s status=%s duration_ms=%s request_id=%s",
            request.method,
            request.path,
            response.status_code,
            duration_ms,
            request_id,
        )
        return response

    @app.errorhandler(404)
    def not_found(_error):
        return jsonify({
            "success": False,
            "error": {"message": "Endpoint not found", "code": 404},
        }), 404

    @app.errorhandler(429)
    def rate_limited(_error):
        return jsonify({
            "success": False,
            "error": {"message": "Rate limit exceeded", "code": 429},
        }), 429

    @app.errorhandler(Exception)
    def unhandled_error(error):
        logger.exception("unhandled_error request_id=%s", getattr(request, "request_id", None))
        return jsonify({
            "success": False,
            "error": {"message": "Internal server error", "code": 500},
        }), 500

    @app.route("/")
    def home():
        return jsonify({
            "success": True,
            "service": "Security Policy Enforcement Engine",
            "status": "ok",
        })

    @app.route("/health")
    def health():
        return jsonify({
            "success": True,
            "service": "Security Policy Enforcement Engine",
            "status": "ok",
            "rate_limit": "30 per minute",
        })

    return app


app = create_app()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
