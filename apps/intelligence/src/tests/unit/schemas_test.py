import pytest
from pydantic import ValidationError

from schemas.forecast_request import ForecastRequest
from schemas.forecast_response import ChronosForecastResponse


def test_forecast_request_accepts_valid_payload():
    request = ForecastRequest(
        forecast_horizon=2,
        timestamps=["2026-08-01T00:00:00Z", "2026-08-02T00:00:00Z"],
        values=[10.0, 12.5],
        model="chronos_univariate",
    )

    assert request.forecast_horizon == 2
    assert request.timestamps == ["2026-08-01T00:00:00Z", "2026-08-02T00:00:00Z"]
    assert request.values == [10.0, 12.5]
    assert request.model == "chronos_univariate"


def test_forecast_request_defaults_model_to_none_when_omitted():
    request = ForecastRequest(
        forecast_horizon=1,
        timestamps=["2026-08-01T00:00:00Z"],
        values=[10.0],
    )

    assert request.model is None


def test_forecast_request_rejects_missing_forecast_horizon():
    with pytest.raises(ValidationError):
        ForecastRequest(
            timestamps=["2026-08-01T00:00:00Z"],
            values=[10.0],
        )


def test_forecast_request_rejects_missing_timestamps():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=1,
            values=[10.0],
        )


def test_forecast_request_rejects_missing_values():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=1,
            timestamps=["2026-08-01T00:00:00Z"],
        )


def test_forecast_request_rejects_non_integer_forecast_horizon():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon="two",
            timestamps=["2026-08-01T00:00:00Z"],
            values=[10.0],
        )


def test_forecast_request_rejects_non_positive_forecast_horizon():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=0,
            timestamps=["2026-08-01T00:00:00Z"],
            values=[10.0],
        )


def test_forecast_request_rejects_empty_timestamps():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=1,
            timestamps=[],
            values=[10.0],
        )


def test_forecast_request_rejects_empty_values():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=1,
            timestamps=["2026-08-01T00:00:00Z"],
            values=[],
        )


def test_forecast_request_rejects_mismatched_timestamp_and_value_lengths():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=1,
            timestamps=["2026-08-01T00:00:00Z", "2026-08-02T00:00:00Z"],
            values=[10.0],
        )


def test_forecast_request_rejects_invalid_timestamp_format():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=1,
            timestamps=["not-a-timestamp"],
            values=[10.0],
        )


def test_forecast_request_rejects_non_numeric_values():
    with pytest.raises(ValidationError):
        ForecastRequest(
            forecast_horizon=1,
            timestamps=["2026-08-01T00:00:00Z"],
            values=["not-a-number"],
        )


def test_chronos_forecast_response_accepts_quantile_fields():
    response = ChronosForecastResponse(
        forecast=[11.0, 12.0],
        timestamps=["2026-08-03T00:00:00Z", "2026-08-04T00:00:00Z"],
        q1=[10.0, 11.0],
        q3=[12.0, 13.0],
    )

    assert response.forecast == [11.0, 12.0]
    assert response.timestamps == ["2026-08-03T00:00:00Z", "2026-08-04T00:00:00Z"]
    assert response.q1 == [10.0, 11.0]
    assert response.q3 == [12.0, 13.0]


def test_chronos_forecast_response_rejects_mismatched_forecast_and_timestamp_lengths():
    with pytest.raises(ValidationError):
        ChronosForecastResponse(
            forecast=[11.0],
            timestamps=["2026-08-03T00:00:00Z", "2026-08-04T00:00:00Z"],
            q1=[10.0],
            q3=[12.0],
        )


def test_chronos_forecast_response_rejects_mismatched_quantile_lengths():
    with pytest.raises(ValidationError):
        ChronosForecastResponse(
            forecast=[11.0, 12.0],
            timestamps=["2026-08-03T00:00:00Z", "2026-08-04T00:00:00Z"],
            q1=[10.0],
            q3=[12.0, 13.0],
        )
