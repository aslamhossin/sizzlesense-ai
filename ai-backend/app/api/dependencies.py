"""FastAPI Dependency Injection (`Depends`) providers for SizzleSense AI services."""

from __future__ import annotations

from functools import lru_cache
from fastapi import Depends

from app.config import SETTINGS
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.learner_store import LearnerMemoryStore
from app.evals.human_learning_eval import HumanLearningEvalRunner


@lru_cache(maxsize=1)
def get_memory_store() -> LearnerMemoryStore:
    """Provides the shared SQLite-backed longitudinal learner session store singleton."""
    return LearnerMemoryStore(db_path=SETTINGS.sqlite_db_path)


def get_cognitive_pipeline(
    memory_store: LearnerMemoryStore = Depends(get_memory_store),
) -> CognitiveCoachingPipeline:
    """Provides the 4-stage cognitive coaching pipeline injected with the learner store."""
    return CognitiveCoachingPipeline(memory_store=memory_store)


@lru_cache(maxsize=1)
def get_eval_runner() -> HumanLearningEvalRunner:
    """Provides the automated 10-trajectory human learning evaluation runner."""
    return HumanLearningEvalRunner()
