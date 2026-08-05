from schemas.forecast_response import ChronosForecastResponse
from schemas.forecast_request import ForecastRequest
from schemas.forecast_response import ForecastResponse
from models.sherpa_model import SherpaModel
import torch
import os
import pandas as pd
import numpy as np
from chronos import BaseChronosPipeline, Chronos2Pipeline
import datetime
import logging
from abc import ABC

# maybe filter out huggingface urls
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)

logger = logging.getLogger(__name__)

class ChronosModel(SherpaModel, ABC):
    def __init__(self):
        device = "cpu"

        if (torch.cuda.is_available()):
            # Need to confirm whether setting env var is necessary
            os.environ["CUDA_VISIBLE_DEVICES"] = "0"
            device = "cuda"

        logger.info("Loading Chronos-2 model")
        self.pipeline: Chronos2Pipeline = BaseChronosPipeline.from_pretrained("amazon/chronos-2", device_map=device)
        logger.info("Loaded Chronos-2 model")

    def forecast(self, context: ForecastRequest) -> ForecastResponse:
        """
        ChronosModel is an abstract class defining the model loading behaviour for specialized children that implements
        the prediction methods
        """
        pass

class ChronosUnivariate(ChronosModel):
    def __init__(self):
        super().__init__()
        self._model_id = "chronos_univariate" # NOSONAR member returned by parent class get_model_id method

    def preprocess(self, context: ForecastRequest) -> pd.DataFrame:
        context_df = pd.DataFrame({
            "item_id": "forecast_item",
            "target": context.values,
            "timestamp": context.timestamps
        })

        context_df["timestamp"] = pd.to_datetime(context_df["timestamp"])

        return context_df
    
    def forecast(self, context: ForecastRequest) -> ChronosForecastResponse:
        context_df: pd.DataFrame = self.preprocess(context)
        pred_df = self.pipeline.predict_df(context_df, prediction_length=context.forecast_horizon, quantile_levels=[0.1, 0.5, 0.9])

        timestamps: list[str] = pred_df["timestamp"].dt.strftime("%Y-%m-%dT%H:%M:%S.%f").tolist()

        response: ForecastResponse = ChronosForecastResponse(
            forecast=pred_df["predictions"].tolist(),
            timestamps=timestamps,
            q1=pred_df["0.1"].tolist(),
            q3=pred_df["0.9"].tolist()
        )

        return response

