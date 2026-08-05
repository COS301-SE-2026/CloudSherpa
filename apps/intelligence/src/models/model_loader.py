from models.fake_model import FakeModel

from models.chronos_model import ChronosUnivariate
from models.sherpa_model import SherpaModel


class ModelLoader:
    def __init__(self):
        self.__available_models: dict[str, SherpaModel] = {}
        self.__testing: bool = False

    def load_models(self) -> None:
        if not self.__testing:
            chronos_univariate: ChronosUnivariate = ChronosUnivariate()
            self.__available_models[chronos_univariate.get_model_id()] = (
                chronos_univariate
            )
        else:
            fake_model: SherpaModel = FakeModel
            self.__available_models[fake_model.get_model_id()] = fake_model

    def get_model(self, model_id: str) -> SherpaModel:
        return self.__available_models[model_id]

    def set_testing(self, flag: bool) -> None:
        self.__testing = flag
