from schemas.forecast_response import ForecastResponse
from schemas.forecast_request import ForecastRequest
from abc import ABC, abstractmethod

class SherpaModel(ABC):

    _model_id: str

    @abstractmethod
    def forecast(self, context: ForecastRequest) -> ForecastResponse:
        """All models must define how to predict a series"""
        pass

    def get_model_id(self) -> str:
        return self._model_id