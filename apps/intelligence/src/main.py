from fastapi import FastAPI
from contextlib import asynccontextmanager
import gc
import torch
from schemas.forecast_request import ForecastRequest
from models.chronos_model import ChronosUnivariate
from models.sherpa_model import SherpaModel

@asynccontextmanager
async def lifespan(app: FastAPI):
    model: SherpaModel = ChronosUnivariate()
    app.state.model = model

    try:
        yield 
    finally:
        app.state.model = None
        del model

        gc.collect()

app = FastAPI(lifespan=lifespan)    

@app.post("/forecast")
async def root(request: ForecastRequest):
    model = app.state.model
    return model.predict_series(series=request.series[0], prediction_length=request.prediction_length)
