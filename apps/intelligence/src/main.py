import gc
import os
import secrets
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Security, status
from fastapi.security import APIKeyHeader

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

API_KEY = os.environ["API_KEY"]

api_key_header = APIKeyHeader(name="X-API-Key")

def verify_api_key(api_key: str = Security(api_key_header)):
    if not secrets.compare_digest(api_key, API_KEY):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED
        )

@app.post("/forecast-chronos", dependencies=[Security(verify_api_key)])
async def root(request: ForecastRequest) -> ChronosForecastResponse:
    model = app.state.models.get_model("chronos_univariate")
    return model.forecast(request)

@app.get("/health")
async def health():
    return { "status": "ok" }