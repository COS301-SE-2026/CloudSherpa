import importlib
import sys
import types

from fastapi.testclient import TestClient


class FakeModel:
    def forecast(self, request):
        return {
            "forecast": [12.0, 13.0],
            "timestamps": ["2026-08-19T00:00:00", "2026-08-19T01:00:00"],
            "q1": [10.0, 11.0],
            "q3": [14.0, 15.0],
        }


class FakeModelLoader:
    def load_models(self):
        pass

    def get_model(self, model_id):
        assert model_id == "chronos_univariate"
        return FakeModel()

def test_forecast_chronos_rejects_invalid_api_key(monkeypatch):

    fake_loader_module = types.ModuleType("models.model_loader")
    fake_loader_module.ModelLoader = FakeModelLoader
    monkeypatch.setitem(sys.modules, "models.model_loader", fake_loader_module)

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