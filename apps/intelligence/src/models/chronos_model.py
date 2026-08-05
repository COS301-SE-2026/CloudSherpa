import logging
import os
from abc import ABC

import pandas as pd
import torch
from chronos import BaseChronosPipeline, Chronos2Pipeline

from models.sherpa_model import SherpaModel
from schemas.forecast_request import ForecastSeries

# maybe filter out huggingface urls
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)

logger = logging.getLogger(__name__)


class ChronosModel(SherpaModel, ABC):
    def __init__(self):
        device = "cpu"

        if torch.cuda.is_available():
            # Need to confirm whether setting env var is necessary
            os.environ["CUDA_VISIBLE_DEVICES"] = "0"
            device = "cuda"

        logger.info("Loading Chronos-2 model")
        self.pipeline: Chronos2Pipeline = BaseChronosPipeline.from_pretrained(
            "amazon/chronos-2", device_map=device
        )
        logger.info("Loaded Chronos-2 model")

    def predict_series(
        self, series: ForecastSeries, prediction_length: int
    ) -> list[float]:
        """
        ChronosModel is an abstract class defining the model loading behaviour for specialized children that implements
        the prediction methods
        """


class ChronosUnivariate(ChronosModel):
    def __init__(self):
        super().__init__()

    def preprocess(self, series: ForecastSeries) -> pd.DataFrame:
        context_df = pd.DataFrame(
            {
                "item_id": series.resource_id,
                "target": series.values,
                "timestamp": series.timestamps,
            }
        )

        context_df["timestamp"] = pd.to_datetime(context_df["timestamp"])

        return context_df

    def predict_series(
        self, series: ForecastSeries, prediction_length: int
    ) -> list[float]:
        context_df: pd.DataFrame = self.preprocess(series)
        pred_df = self.pipeline.predict_df(
            context_df,
            prediction_length=prediction_length,
            quantile_levels=[0.1, 0.5, 0.9],
        )

        return pred_df["predictions"].tolist()
