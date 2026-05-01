import logging
import time
from uuid import uuid4

from flask import Flask, jsonify, request
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

from routes.ai import ai_bp

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
        default_limits=["100 per hour"],
        storage_uri="memory://",
    )

    app.register_blueprint(ai_bp)

    @app.before_request
    def log_request():
        request.request_id = str(uuid4())
        request.start_time = time.perf_counter()
        logger.info(
            "api_request method=%s path=%s request_id=%s remote_addr=%s",
            request.method,
            request.path,
            request.request_id,
            request.remote_addr,
        )

    @app.after_request
    def finalize_response(response):
        duration_ms = int((time.perf_counter() - request.start_time) * 1000)
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["Referrer-Policy"] = "no-referrer"
        response.headers["Content-Security-Policy"] = "default-src 'none'; frame-ancestors 'none'"
        response.headers["X-Request-ID"] = request.request_id
        logger.info(
            "api_response method=%s path=%s status=%s duration_ms=%s request_id=%s",
            request.method,
            request.path,
            response.status_code,
            duration_ms,
            request.request_id,
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
        })

    return app


app = create_app()


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)
