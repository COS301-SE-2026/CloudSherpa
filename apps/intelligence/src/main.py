from schemas.forecast_response import ForecastResponse
from models.model_loader import ModelLoader
from fastapi import FastAPI
from contextlib import asynccontextmanager
import gc
from schemas.forecast_request import ForecastRequest

@asynccontextmanager
async def lifespan(app: FastAPI):
    models: ModelLoader = ModelLoader()
    models.load_models()
    app.state.models = models

    try:
        yield 
    finally:
        app.state.models = None
        del model

        gc.collect()

app = FastAPI(lifespan=lifespan)    

@app.post("/forecast")
async def root(request: ForecastRequest) -> ForecastResponse:
    model = app.state.models.get_model("chronos_univariate")
    return model.forecast(request)
