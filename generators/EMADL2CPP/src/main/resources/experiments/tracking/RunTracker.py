from abc import ABC, abstractmethod
from mlflow import MlflowClient
from typing import List
from argparse import Namespace


class RunTracker(ABC):
    @abstractmethod
    def log_metric(self, key, value, timestamp=None, step=None):
        pass


class MultiBackendTracker(RunTracker):
    def __init__(self, args: Namespace):
        self.run_trackers: List[RunTracker] = self.extract_run_trackers(args)

    def extract_run_trackers(self, args: Namespace) -> List[RunTracker]:
        run_trackers = []

        if hasattr(args, "tracking_backends"):
            tracking_backend_string = args.tracking_backends
            if tracking_backend_string is not None:
                for tracking_backend in tracking_backend_string.upper().split(","):
                    if tracking_backend == "MLFLOW":
                        run_trackers.append(MLflowTracker(args))
                    elif tracking_backend == "STDOUT":
                        run_trackers.append(StdOutTracker(args))
                    else:
                        raise ValueError("Unknown tracking backend: {}".format(tracking_backend))
        return run_trackers

    def log_metric(self, key, value, timestamp=None, step=None):
        for run_tracker in self.run_trackers:
            run_tracker.log_metric(key, value, timestamp, step)


class MLflowTracker(RunTracker):
    def __init__(self, args: Namespace):
        self.run_id = args.mlflow_run_id
        self.tracking_uri = args.mlflow_tracking_uri
        self.client = MlflowClient(self.tracking_uri)

    def log_metric(self, key, value, timestamp=None, step=None):
        self.client.log_metric(self.run_id, key, value, timestamp, step)


class StdOutTracker(RunTracker):
    def __init__(self, args: Namespace):
        self.experiment_name = args.stdout_experiment_name
        print("Logging to stdout with experiment name: {}".format(self.experiment_name))

    def log_metric(self, key, value, timestamp=None, step=None):
        print("Logging metric: {} = {} at step {}".format(key, value, step))
