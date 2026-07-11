from sherpa_model import SherpaModel
from schemas.forecast_request import ForecastSeries

import os

os.environ["CUDA_VISIBLE_DEVICES"] = "0"

import pandas as pd
import numpy as np
from chronos import BaseChronosPipeline, Chronos2Pipeline
import datetime

class ChronosModel(SherpaModel):
    def __init__(self):
        pipeline: Chronos2Pipeline = BaseChronosPipeline.from_pretrained("amazon/chronos-2", device_map="cuda")

    # def chronos_preprocess(self):
