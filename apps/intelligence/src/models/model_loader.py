from models.chronos_model import ChronosUnivariate
from models.sherpa_model import SherpaModel
class ModelLoader:
    __available_models: dict[str, SherpaModel]

    def load_models(self) -> None:
        chronos_univariate: ChronosUnivariate = ChronosUnivariate()
        self.__available_models[chronos_univariate.get_model_id()] = chronos_univariate
    
    def get_model(self, model_id: str) -> SherpaModel:
        return self.__available_models[model_id]