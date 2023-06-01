import mlflow
import torch
from torch.utils.data import Dataset as torch_dataset
from torch.utils.data import DataLoader
from pathlib import Path
import yaml
from typing import Union
import pandas as pd
import scipy
import numpy as np

from giskard.core.core import SupportedModelTypes
from giskard.core.model import Model
from giskard.path_utils import get_size
class TorchMinimalDataset(torch_dataset):
    def __init__(self, df: pd.DataFrame):
        self.entries = df

    def __len__(self):
        return len(self.entries)

    def __getitem__(self, idx):
        return self.entries.iloc[idx]


class PyTorchModel(Model):
    def __init__(self,
                 clf,
                 model_type: Union[SupportedModelTypes, str],
                 device='cpu',
                 name: str = None,
                 data_preprocessing_function=None,
                 model_postprocessing_function=None,
                 feature_names=None,
                 classification_threshold=0.5,
                 classification_labels=None,
                 loader_module:str = 'giskard.models.pytorch',
                 loader_class:str = 'PyTorchModel') -> None:

        super().__init__(clf, model_type, name, data_preprocessing_function, model_postprocessing_function,
                         feature_names, classification_threshold, classification_labels, loader_module, loader_class)
        self.device=device

    @classmethod
    def read_model_from_local_dir(cls, local_path):
        return mlflow.pytorch.load_model(local_path)


    def save_to_local_dir(self, local_path):

        info = self._new_mlflow_model_meta()
        mlflow.pytorch.save_model(self.clf,
                                  path=local_path,
                                  mlflow_model=info)
        with open(Path(local_path) / 'giskard-model-meta.yaml', 'w') as f:
            yaml.dump(
                {
                    "language_version": info.flavors['python_function']['python_version'],
                    "loader_module": self.meta.loader_module,
                    "loader_class": self.meta.loader_class,
                    "language": "PYTHON",
                    "model_type": self.meta.model_type.name.upper(),
                    "threshold": self.meta.classification_threshold,
                    "feature_names": self.meta.feature_names,
                    "classification_labels": self.meta.classification_labels,
                    "id": info.model_uuid,
                    "name": self.meta.name,
                    "size": get_size(local_path),
                }, f, default_flow_style=False)

        return info

    def _raw_predict(self, data):
        self.clf.to(self.device)
        self.clf.eval()
        predictions=[]

        # Use isinstance to check if o is an instance of X or any subclass of X
        # Use is to check if the type of o is exactly X, excluding subclasses of X
        if isinstance(data, pd.DataFrame):
            data = TorchMinimalDataset(data)
        elif not isinstance(data, torch_dataset) and not isinstance(data, DataLoader):
            with torch.no_grad():
                predictions = self.clf(*data)
                predictions = np.array(predictions.detach().numpy())

        if not predictions:
            with torch.no_grad():
                for entry in data:
                    predictions.append(self.clf(*entry).detach().numpy()[0])
            predictions = np.array(predictions)

        if self.model_postprocessing_function:
            predictions = self.model_postprocessing_function(predictions)

        return predictions.squeeze()




