import importlib
import sys
import types

import pytest


class FakeChronosUnivariate:
    instance_count = 0

    def __init__(self):
        FakeChronosUnivariate.instance_count += 1
        self._model_id = "chronos_univariate"

    def get_model_id(self):
        return self._model_id


@pytest.fixture
def model_loader_module(monkeypatch):
    """Mock chronos module ChronosUnivariate class to test model loading in subsequent tests"""
    FakeChronosUnivariate.instance_count = 0

    fake_chronos_module = types.ModuleType("models.chronos_model")
    fake_chronos_module.ChronosUnivariate = FakeChronosUnivariate
    monkeypatch.setitem(sys.modules, "models.chronos_model", fake_chronos_module)

    sys.modules.pop("models.model_loader", None)
    module = importlib.import_module("models.model_loader")

    yield module

    sys.modules.pop("models.model_loader", None)


def test_model_loader_starts_without_loaded_models(model_loader_module):
    loader = model_loader_module.ModelLoader()

    with pytest.raises(KeyError):
        loader.get_model("chronos_univariate")


def test_model_loader_load_models_registers_chronos_univariate_model(
    model_loader_module,
):
    loader = model_loader_module.ModelLoader()

    loader.load_models()

    assert loader.get_model("chronos_univariate").get_model_id() == "chronos_univariate"


def test_model_loader_get_model_returns_registered_model_by_id(model_loader_module):
    loader = model_loader_module.ModelLoader()

    loader.load_models()
    model = loader.get_model("chronos_univariate")

    assert isinstance(model, FakeChronosUnivariate)


def test_model_loader_get_model_raises_key_error_for_unknown_model_id(
    model_loader_module,
):
    loader = model_loader_module.ModelLoader()

    loader.load_models()

    with pytest.raises(KeyError):
        loader.get_model("unknown_model")


def test_model_loader_load_models_overwrites_existing_model_id(model_loader_module):
    loader = model_loader_module.ModelLoader()

    loader.load_models()
    first_model = loader.get_model("chronos_univariate")
    loader.load_models()
    second_model = loader.get_model("chronos_univariate")

    assert first_model is not second_model
    assert FakeChronosUnivariate.instance_count == 2
