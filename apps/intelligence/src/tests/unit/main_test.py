import importlib
import sys

from fastapi.testclient import TestClient


def test_forecast_chronos_rejects_invalid_api_key(monkeypatch):
    monkeypatch.setenv("API_KEY", "test-key")

    # import main after monkeypatch set API key
    sys.modules.pop("main", None)
    main = importlib.import_module("main")

    payload = {
        "forecast_horizon": 1,
        "timestamps": ["2026-08-19T00:00:00"],
        "values": [10.0],
    }

    with TestClient(main.app) as client:
        response = client.post(
            "/forecast-chronos",
            json=payload,
            headers={"X-API-Key": "wrong-key"},
        )

    assert response.status_code == 401