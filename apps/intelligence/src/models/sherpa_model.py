from abc import ABC, abstractmethod
from schemas.forecast_request import ForecastSeries

class SherpaModel(ABC):
    @abstractmethod
    def predict_series(self, series: ForecastSeries, prediction_length: int) -> list[float]:
        """All models must define how to predict a series"""
        pass
