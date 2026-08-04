from pydantic import BaseModel

class ForecastResponse(BaseModel):
    forecast: list[float]
    timestamps: list[str]

class ChronosForecastResponse(ForecastResponse):
    q1: list[float]
    q3: list[float]