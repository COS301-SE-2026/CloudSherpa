import gc
from contextlib import asynccontextmanager

from fastapi import FastAPI

from models.model_loader import ModelLoader
from schemas.forecast_request import ForecastRequest
from schemas.forecast_response import ChronosForecastResponse


@asynccontextmanager
async def lifespan(app: FastAPI):
    models: ModelLoader = ModelLoader()
    models.load_models()
    app.state.models = models

    try:
        yield
    finally:
        app.state.models = None
        del models

        gc.collect()


app = FastAPI(lifespan=lifespan)


@app.post("/forecast-chronos")
async def root(request: ForecastRequest) -> ChronosForecastResponse:
    model = app.state.models.get_model("chronos_univariate")
    return model.forecast(request)
