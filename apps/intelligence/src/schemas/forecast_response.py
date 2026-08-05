from typing import Self

from pydantic import BaseModel, model_validator


class ForecastResponse(BaseModel):
    forecast: list[float]
    timestamps: list[str]

    @model_validator(mode="after")
    def validate_forecast(self) -> Self:
        if len(self.forecast) != len(self.timestamps):
            raise ValueError("forecast and timestamps lengths should be uniform")

        return self


class ChronosForecastResponse(ForecastResponse):
    q1: list[float]
    q3: list[float]

    @model_validator(mode="after")
    def validate_quantiles(self) -> Self:
        if len(self.q1) != len(self.q3):
            raise ValueError("Quantile lengths should be uniform")

        return self
