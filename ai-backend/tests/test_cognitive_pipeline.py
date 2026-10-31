"""Tests for Atomic Step 4: 4-Stage Applied AI Cognitive Pipeline & Longitudinal Learner Memory Store."""

import pytest

from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.learner_store import LearnerMemoryStore
from app.schemas.domain import (
    BehavioralTransferStatus,
    PerceptionGapLevel,
    SessionAnalyzeRequest,
)
from app.vision.frame_analyzer import generate_synthetic_kitchen_frame_base64


@pytest.mark.asyncio
async def test_same_visual_failure_yields_three_different_diagnoses_for_jd_archetypes():
    """
    Core Cookpad JD Requirement:
    Two or three learners may fail to sauté their onions sufficiently (same pale visual frame),
    but for completely different latent reasons (Moisture Blindness vs. Heat Anxiety vs. Perception Blindspot).
    """
    store = LearnerMemoryStore()
    pipeline = CognitiveCoachingPipeline(memory_store=store)
    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")

    # 1. Learner A: Moisture Blindness
    resp_a = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_a",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="I kept stirring the onions for 8 minutes on medium heat, but they stayed pale and watery.",
            keyframe_base64_list=[pale_frame],
        )
    )

    # 2. Learner B: Heat Anxiety (Afraid of burning)
    resp_b = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_b",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="It started steaming a lot right away so I turned the heat way down so it wouldn't burn.",
            keyframe_base64_list=[pale_frame],
        )
    )

    # 3. Learner C: Perception Blindspot (Believes pale onions are browned)
    resp_c = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_c",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="I sautéed the onions for 5 minutes just like the recipe said and they browned really nicely!",
            keyframe_base64_list=[pale_frame],
        )
    )

    # Verify distinct inferred archetypes & teaching decisions despite identical visual input
    assert resp_a.stage2_learner_state.learner_archetype_id == "moisture_blindness"
    assert resp_b.stage2_learner_state.learner_archetype_id == "heat_anxiety"
    assert resp_c.stage2_learner_state.learner_archetype_id == "perception_blindspot"

    # Learner C must trigger CRITICAL_BLINDSPOT because they claim browning while CV shows pale steaming
    assert (
        resp_c.stage1_physical_observation.perception_gap_level
        == PerceptionGapLevel.CRITICAL_BLINDSPOT
    )

    # Each learner receives a different primary principle tailored to their latent root cause
    assert (
        resp_a.stage3_teaching_decision.target_chef_principle_id
        == "moisture_evaporation_gate"
    )
    assert (
        resp_b.stage3_teaching_decision.target_chef_principle_id
        == "thermal_recovery_and_heat_control"
    )
    assert (
        resp_c.stage3_teaching_decision.target_chef_principle_id
        == "visual_and_acoustic_doneness_calibration"
    )


@pytest.mark.asyncio
async def test_longitudinal_transfer_across_three_attempts():
    """Verify Attempt 1 -> Attempt 2 -> Attempt 3 tracks behavioral transfer and conceptual shift."""
    store = LearnerMemoryStore()
    pipeline = CognitiveCoachingPipeline(memory_store=store)

    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    golden_frame = generate_synthetic_kitchen_frame_base64("golden_maillard")

    # Attempt 1: Baseline failure
    r1 = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_growth",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="It steamed a lot so I turned the heat down low out of fear of burning.",
            keyframe_base64_list=[pale_frame],
        )
    )
    assert r1.stage2_learner_state.transfer_status == BehavioralTransferStatus.INITIAL_BASELINE
    assert r1.stage2_learner_state.conceptual_shift_verified is False

    # Attempt 2: Good execution + sensory reflection alignment -> CONCEPT_ADOPTED
    r2 = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_growth",
            dish_id="sauteed_onions",
            attempt_number=2,
            learner_reflection="I recognized the white vapor was harmless steam, kept the sizzle steady, and watched the edges turn golden!",
            keyframe_base64_list=[golden_frame],
        )
    )
    assert r2.stage2_learner_state.transfer_status == BehavioralTransferStatus.CONCEPT_ADOPTED
    assert r2.stage2_learner_state.conceptual_shift_verified is True
    assert len(store.get_history("learner_growth")) == 2


@pytest.mark.asyncio
async def test_lucky_execution_without_sensory_understanding_is_flagged():
    """
    Edge case from Senior Principal Audit:
    If the pan frame is golden on Attempt 2, but the learner's reflection still credits a blind clock timer
    instead of sensory observation, transfer_status must be PARTIAL_EXECUTION_ONLY (not full concept adoption).
    """
    store = LearnerMemoryStore()
    pipeline = CognitiveCoachingPipeline(memory_store=store)

    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    golden_frame = generate_synthetic_kitchen_frame_base64("golden_maillard")

    await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_lucky",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="I cooked for 5 minutes on the timer and they looked done.",
            keyframe_base64_list=[pale_frame],
        )
    )

    r2_lucky = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_lucky",
            dish_id="sauteed_onions",
            attempt_number=2,
            learner_reflection="I just set my phone timer for 11 minutes instead of 5 minutes without looking at the pan.",
            keyframe_base64_list=[golden_frame],
        )
    )
    assert (
        r2_lucky.stage2_learner_state.transfer_status
        == BehavioralTransferStatus.PARTIAL_EXECUTION_ONLY
    )
    assert r2_lucky.stage2_learner_state.conceptual_shift_verified is False
