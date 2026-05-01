import logging

from flask import Flask, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

from routes.report import report_bp

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s %(message)s",
)


def create_app():
    app = Flask(__name__)

    Limiter(
        get_remote_address,
        app=app,
        default_limits=["100 per hour"],
        storage_uri="memory://",
    )

    app.register_blueprint(report_bp)

    @app.route("/")
    def home():
        return jsonify({
            "status": "ok",
            "service": "AI Security Engine",
        })

    @app.route("/health")
    def health():
        return jsonify({
            "status": "ok",
            "service": "AI Security Engine",
        })

    return app


app = create_app()

if __name__ == "__main__":
    app.run(port=5000, debug=False)
