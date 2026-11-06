"""Cooking session analysis, longitudinal learner history, and archetype comparison router."""

from __future__ import annotations

from typing import List
from fastapi import APIRouter, Depends

from app.api.dependencies import get_cognitive_pipeline, get_memory_store
from app.engine.chef_knowledge import ONION_LEARNER_ARCHETYPES
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.learner_store import LearnerMemoryStore
from app.schemas.api_models import (
    ArchetypeComparisonItem,
    ArchetypeComparisonResponse,
    LearnerHistoryResponse,
)
from app.schemas.domain import CognitiveCoachingResponse, SessionAnalyzeRequest
from app.vision.frame_analyzer import generate_synthetic_kitchen_frame_base64

router = APIRouter(tags=["Cognitive Sessions & Learners"])


@router.post("/sessions/analyze", response_model=CognitiveCoachingResponse)
async def analyze_cooking_session(
    request: SessionAnalyzeRequest,
    pipeline: CognitiveCoachingPipeline = Depends(get_cognitive_pipeline),
) -> CognitiveCoachingResponse:
    """Runs the 4-stage Applied AI cognitive pipeline on a kitchen session."""
    return await pipeline.analyze_session(request)


@router.get(
    "/learners/{learner_id}/history", response_model=LearnerHistoryResponse
)
async def get_learner_history(
    learner_id: str,
    memory_store: LearnerMemoryStore = Depends(get_memory_store),
) -> LearnerHistoryResponse:
    """Returns longitudinal session history (Attempt 1 -> 2 -> 3) for a learner."""
    sessions = memory_store.get_history(learner_id)
    return LearnerHistoryResponse(learner_id=learner_id, sessions=sessions)


@router.get(
    "/archetypes/compare-onion-learners",
    response_model=ArchetypeComparisonResponse,
)
async def compare_three_onion_learners() -> ArchetypeComparisonResponse:
    """
    Demonstrates latent root-cause disambiguation:
    3 learners all fail to sauté onions sufficiently (identical pale visual frame),
    but receive 3 distinct latent diagnoses and single-issue coaching interventions.
    """
    temp_pipeline = CognitiveCoachingPipeline(memory_store=LearnerMemoryStore())
    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    items: List[ArchetypeComparisonItem] = []

    for archetype_id, archetype in ONION_LEARNER_ARCHETYPES.items():
        preset = archetype.attempts[1]
        diagnosis = await temp_pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id=f"compare_{archetype_id}",
                dish_id="sauteed_onions",
                attempt_number=1,
                learner_reflection=preset.learner_reflection,
                archetype_hint=archetype_id,
                keyframe_base64_list=[pale_frame],
                acoustic_hint=preset.acoustic_signature,
            )
        )
        items.append(
            ArchetypeComparisonItem(
                archetype_id=archetype_id,
                display_name=archetype.display_name,
                pedagogical_scenario_summary=archetype.pedagogical_scenario_summary,
                attempt1_diagnosis=diagnosis,
            )
        )

    return ArchetypeComparisonResponse(
        shared_visual_input_description=(
            "Identical overhead frame: pale translucent onions pooling in liquid (Maillard browning ratio < 0.20)."
        ),
        comparisons=items,
    )
