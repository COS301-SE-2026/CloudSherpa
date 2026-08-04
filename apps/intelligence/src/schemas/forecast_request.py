from pydantic import BaseModel

class ForecastRequest(BaseModel):
    forecast_horizon: int 
    timestamps: list[str]
    values: list[float]
    model: str | None = None