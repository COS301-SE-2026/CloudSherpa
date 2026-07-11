from fastapi import FastAPI
from src.schemas.forecast_request import ForecastRequest

app = FastAPI()    

@app.post("/forecast")
async def root(request: ForecastRequest):
    return {
        "message": "Received forecast request",
        "series_len": len(request.series),
        "prediction_len": request.prediction_length
    }
