from pydantic import BaseModel

class ForecastSeries(BaseModel):
    series_id: str
    provider: str
    resource_id: str 
    resource_type: str 
    metric_type: str 
    # Put frequency on ice for now
    # frequency: str
    timestamps: list[str]
    values: list[float]

class ForecastRequest(BaseModel):
    series: list[ForecastSeries]
    prediction_length: int