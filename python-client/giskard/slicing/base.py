import pandas as pd
from typing import Sequence


class BaseSlicer:
    def __init__(
        self,
        data: pd.DataFrame,
        features: Sequence[str] | None = None,
        target: str | None = None,
        min_deviation: float = 0.1,
        abs_deviation: bool = False,
    ):
        self.data = data
        self.features = features
        self.target = target
        self.min_deviation = min_deviation
        self.abs_deviation = abs_deviation

    def find_slices(self):
        raise NotImplementedError()
