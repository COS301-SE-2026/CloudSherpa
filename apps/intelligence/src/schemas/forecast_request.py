from datetime import datetime
from typing import Self

from pydantic import BaseModel, field_validator, model_validator



class ForecastRequest(BaseModel):
    forecast_horizon: int
    timestamps: list[str]
    values: list[float]
    model: str | None = None

    @field_validator("forecast_horizon")
    @classmethod
    def validate_forecast_horizon(cls, horizon: int) -> int:
        if horizon <= 0:
            raise ValueError("Forecast Horizon must be greater than 0")

        return horizon

    @field_validator("timestamps")
    @classmethod
    def validate_timestamps(cls, timestamps: list[str]) -> list[str]:
        if len(timestamps) <= 0:
            raise ValueError("The timestamp list cannot be empty")

        for timestamp in timestamps:
            datetime.fromisoformat(timestamp)

        return timestamps

    @field_validator("values")
    @classmethod
    def validate_values(cls, values: list[float]) -> list[float]:
        if len(values) <= 0:
            raise ValueError("The values list cannot be empty")

        return values

    @model_validator(mode="after")
    def validate_matching_lengths(self) -> Self:
        if len(self.timestamps) != len(self.values):
            raise ValueError("Timestamps and value list should be of the same length")

        return self
