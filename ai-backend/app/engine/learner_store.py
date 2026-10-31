"""In-memory longitudinal learner profile and attempt history store."""

from __future__ import annotations

from typing import Dict, List, Optional

from app.schemas.domain import CognitiveCoachingResponse


class LearnerMemoryStore:
    """Stores longitudinal session records across Attempts 1 -> 2 -> 3 per learner."""

    def __init__(self) -> None:
        self._history_by_learner: Dict[str, List[CognitiveCoachingResponse]] = {}

    def save_session(
        self, learner_id: str, response: CognitiveCoachingResponse
    ) -> None:
        sessions = self._history_by_learner.setdefault(learner_id, [])
        # Replace if same attempt_number & dish_id already exists, otherwise append
        sessions = [
            s
            for s in sessions
            if not (
                s.dish_id == response.dish_id
                and s.attempt_number == response.attempt_number
            )
        ]
        sessions.append(response)
        sessions.sort(key=lambda s: s.attempt_number)
        self._history_by_learner[learner_id] = sessions

    def get_history(
        self, learner_id: str, dish_id: Optional[str] = None
    ) -> List[CognitiveCoachingResponse]:
        sessions = list(self._history_by_learner.get(learner_id, []))
        if dish_id:
            sessions = [s for s in sessions if s.dish_id == dish_id]
        return sessions

    def get_previous_attempt(
        self, learner_id: str, dish_id: str, current_attempt: int
    ) -> Optional[CognitiveCoachingResponse]:
        history = self.get_history(learner_id, dish_id)
        prior = [s for s in history if s.attempt_number < current_attempt]
        return prior[-1] if prior else None

    def clear(self) -> None:
        self._history_by_learner.clear()
