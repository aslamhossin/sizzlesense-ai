"""SQLite-backed longitudinal learner profile and attempt history store."""

from __future__ import annotations

from pathlib import Path
import sqlite3
import threading
from typing import List, Optional

from app.schemas.domain import CognitiveCoachingResponse


class LearnerMemoryStore:
    """
    Stores longitudinal session records across Attempts 1 -> 2 -> 3 per learner.

    Supports both isolated in-memory SQLite (`db_path=None` or `":memory:"`) for unit tests
    and file-backed WAL SQLite persistence (`db_path=".../sizzlesense_learners.db"`) for production.
    """

    def __init__(self, db_path: str | Path | None = None) -> None:
        self._lock = threading.RLock()
        if db_path is None or str(db_path) == ":memory:":
            self._db_target = ":memory:"
        else:
            resolved = Path(db_path)
            resolved.parent.mkdir(parents=True, exist_ok=True)
            self._db_target = str(resolved)

        self._conn = sqlite3.connect(self._db_target, check_same_thread=False)
        self._init_schema()

    def _init_schema(self) -> None:
        with self._lock:
            if self._db_target != ":memory:":
                self._conn.execute("PRAGMA journal_mode=WAL;")
            self._conn.execute(
                """
                CREATE TABLE IF NOT EXISTS learner_sessions (
                    learner_id TEXT NOT NULL,
                    dish_id TEXT NOT NULL,
                    attempt_number INTEGER NOT NULL,
                    session_id TEXT NOT NULL,
                    response_json TEXT NOT NULL,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (learner_id, dish_id, attempt_number)
                )
                """
            )
            self._conn.commit()

    def save_session(
        self, learner_id: str, response: CognitiveCoachingResponse
    ) -> None:
        payload_json = response.model_dump_json()
        with self._lock:
            self._conn.execute(
                """
                INSERT INTO learner_sessions (
                    learner_id, dish_id, attempt_number, session_id, response_json, updated_at
                )
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(learner_id, dish_id, attempt_number) DO UPDATE SET
                    session_id = excluded.session_id,
                    response_json = excluded.response_json,
                    updated_at = CURRENT_TIMESTAMP
                """,
                (
                    learner_id,
                    response.dish_id,
                    response.attempt_number,
                    response.session_id,
                    payload_json,
                ),
            )
            self._conn.commit()

    def get_history(
        self, learner_id: str, dish_id: Optional[str] = None
    ) -> List[CognitiveCoachingResponse]:
        with self._lock:
            if dish_id:
                cursor = self._conn.execute(
                    """
                    SELECT response_json
                    FROM learner_sessions
                    WHERE learner_id = ? AND dish_id = ?
                    ORDER BY attempt_number ASC
                    """,
                    (learner_id, dish_id),
                )
            else:
                cursor = self._conn.execute(
                    """
                    SELECT response_json
                    FROM learner_sessions
                    WHERE learner_id = ?
                    ORDER BY attempt_number ASC
                    """,
                    (learner_id,),
                )
            rows = cursor.fetchall()
        return [CognitiveCoachingResponse.model_validate_json(row[0]) for row in rows]

    def get_previous_attempt(
        self, learner_id: str, dish_id: str, current_attempt: int
    ) -> Optional[CognitiveCoachingResponse]:
        with self._lock:
            cursor = self._conn.execute(
                """
                SELECT response_json
                FROM learner_sessions
                WHERE learner_id = ? AND dish_id = ? AND attempt_number < ?
                ORDER BY attempt_number DESC
                LIMIT 1
                """,
                (learner_id, dish_id, current_attempt),
            )
            row = cursor.fetchone()
        if not row:
            return None
        return CognitiveCoachingResponse.model_validate_json(row[0])

    def delete_learner_history(self, learner_id: str) -> int:
        with self._lock:
            cursor = self._conn.execute(
                "DELETE FROM learner_sessions WHERE learner_id = ?",
                (learner_id,),
            )
            self._conn.commit()
            return cursor.rowcount

    def clear(self) -> None:
        with self._lock:
            self._conn.execute("DELETE FROM learner_sessions")
            self._conn.commit()
