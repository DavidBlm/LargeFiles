from dataclasses import dataclass
import pathlib
import typing as t

@dataclass
class Dataset:
    id: str
    path: pathlib.Path
    graphFile: t.Optional[pathlib.Path] = None

@dataclass
class TrainingDataset(Dataset):
    retraining: bool = True

@dataclass
class RetrainingConf:
    testing: Dataset
    changes: t.List[TrainingDataset]