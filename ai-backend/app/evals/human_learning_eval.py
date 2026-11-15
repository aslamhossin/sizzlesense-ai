"""Automated Human Learning Evaluation Suite (`EvalBench`) running live Gemini coaching."""

from __future__ import annotations

import re
from typing import List
from pydantic import BaseModel, Field

from app.audio.sizzle_analyzer import generate_synthetic_sizzle_wav_base64
from app.engine.chef_knowledge import ONION_LEARNER_ARCHETYPES
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.learner_store import LearnerMemoryStore
from app.schemas.domain import (
    AcousticSizzleSignature,
    BehavioralTransferStatus,
    PerceptionGapLevel,
    SessionAnalyzeRequest,
)

_SIGNATURE_TO_WAV_REGIME = {
    AcousticSizzleSignature.SILENT_COLD: "silent_cold",
    AcousticSizzleSignature.WET_STEAM_BUBBLE: "wet_steam_bubble",
    AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE: "steady_applause_sizzle",
    AcousticSizzleSignature.AGGRESSIVE_POPPING_BURN: "aggressive_popping_burn",
}


class EvalCaseScore(BaseModel):
    """Detailed evaluation score for a single learner session."""

    case_id: str
    archetype_id: str
    attempt_number: int
    perception_gap_passed: bool
    root_cause_passed: bool
    single_issue_restraint_passed: bool
    sensory_grounding_score: float = Field(..., ge=0.0, le=1.0)
    transfer_tracking_passed: bool
    summary_note: str


class EvalSuiteReport(BaseModel):
    """Aggregate benchmark report across all evaluated learners and attempts."""

    total_sessions_evaluated: int
    perception_gap_detection_score: float = Field(..., ge=0.0, le=1.0)
    latent_root_cause_accuracy: float = Field(..., ge=0.0, le=1.0)
    single_issue_restraint_score: float = Field(..., ge=0.0, le=1.0)
    sensory_grounding_score: float = Field(..., ge=0.0, le=1.0)
    cross_attempt_transfer_score: float = Field(..., ge=0.0, le=1.0)
    overall_human_learning_score: float = Field(..., ge=0.0, le=1.0)
    case_results: List[EvalCaseScore]


def score_sensory_grounding_with_penalty(coaching_message: str) -> float:
    """
    Evaluates whether coaching teaches sensory observation (visual + auditory sizzle/aroma)
    and penalizes rigid timer/recipe prescriptions ('cook for exactly X minutes').
    """
    text = coaching_message.lower()

    visual_cues = [
        "watch",
        "look",
        "golden",
        "amber",
        "shimmer",
        "translucent",
        "crust",
        "fond",
        "steam",
        "oil",
        "smoke",
    ]
    auditory_cues = [
        "listen",
        "sizzle",
        "sound",
        "bubbling",
        "hiss",
        "quiet",
        "applause",
        "crackle",
    ]

    has_visual = any(w in text for w in visual_cues)
    has_auditory = any(w in text for w in auditory_cues)

    score = 0.0
    if has_visual:
        score += 0.5
    if has_auditory:
        score += 0.5

    if re.search(r"cook for (exactly )?\d+\s*minutes", text):
        score -= 0.75

    return round(max(0.0, min(1.0, score)), 3)


class HumanLearningEvalRunner:
    """Runs the 10-session Human Learning Evaluation Benchmark against live Gemini + Audio FFT."""

    async def run_full_benchmark(self) -> EvalSuiteReport:
        store = LearnerMemoryStore()
        pipeline = CognitiveCoachingPipeline(memory_store=store)
        case_results: List[EvalCaseScore] = []

        # 1. Evaluate all 3 Learner Archetypes across Attempts 1 -> 2 -> 3 (9 sessions)
        # No archetype_hint or acoustic_hint leakage: pipeline must infer from reflection + WAV FFT + history.
        for archetype_id, archetype in ONION_LEARNER_ARCHETYPES.items():
            learner_id = f"eval_{archetype_id}"
            for attempt_num in [1, 2, 3]:
                preset = archetype.attempts[attempt_num]
                wav_regime = _SIGNATURE_TO_WAV_REGIME[preset.acoustic_signature]
                wav_b64 = generate_synthetic_sizzle_wav_base64(wav_regime)

                response = await pipeline.analyze_session(
                    SessionAnalyzeRequest(
                        learner_id=learner_id,
                        dish_id="sauteed_onions",
                        attempt_number=attempt_num,
                        learner_reflection=preset.learner_reflection,
                        audio_wav_base64=wav_b64,
                    )
                )

                gap_ok = (
                    response.stage1_physical_observation.perception_gap_level
                    == preset.expected_perception_gap
                )
                root_ok = (
                    response.stage2_learner_state.learner_archetype_id == archetype_id
                )
                restraint_ok = (
                    bool(response.stage3_teaching_decision.selected_primary_issue.strip())
                    and len(response.stage3_teaching_decision.deliberately_omitted_issues)
                    >= 1
                    and len(response.stage3_teaching_decision.candidate_issues_detected)
                    >= 2
                )
                sensory_score = score_sensory_grounding_with_penalty(
                    response.stage4_sensory_coaching.coaching_message
                )

                if attempt_num == 1:
                    transfer_ok = (
                        response.stage2_learner_state.transfer_status
                        == BehavioralTransferStatus.INITIAL_BASELINE
                        and response.stage2_learner_state.conceptual_shift_verified
                        is False
                    )
                else:
                    transfer_ok = (
                        response.stage2_learner_state.transfer_status
                        == BehavioralTransferStatus.CONCEPT_ADOPTED
                        and response.stage2_learner_state.conceptual_shift_verified
                        is True
                    )

                case_results.append(
                    EvalCaseScore(
                        case_id=f"{archetype_id}_attempt_{attempt_num}",
                        archetype_id=archetype_id,
                        attempt_number=attempt_num,
                        perception_gap_passed=gap_ok,
                        root_cause_passed=root_ok,
                        single_issue_restraint_passed=restraint_ok,
                        sensory_grounding_score=sensory_score,
                        transfer_tracking_passed=transfer_ok,
                        summary_note=(
                            f"Model: {response.reasoning_model_used} | "
                            f"Primary: '{response.stage3_teaching_decision.selected_primary_issue}' | "
                            f"Transfer: {response.stage2_learner_state.transfer_status.value}"
                        ),
                    )
                )

        # 2. Negative Control Case #10: Lucky execution via blind timer (honest assertions, zero hardcoded True)
        await pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id="eval_negative_control",
                dish_id="sauteed_onions",
                attempt_number=1,
                learner_reflection="I cooked for 5 minutes and they looked done.",
                audio_wav_base64=generate_synthetic_sizzle_wav_base64(
                    "wet_steam_bubble"
                ),
            )
        )
        neg_resp = await pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id="eval_negative_control",
                dish_id="sauteed_onions",
                attempt_number=2,
                learner_reflection="I just set my phone timer for 11 minutes without looking at the pan.",
                audio_wav_base64=generate_synthetic_sizzle_wav_base64(
                    "steady_applause_sizzle"
                ),
            )
        )
        neg_gap_ok = (
            neg_resp.stage1_physical_observation.perception_gap_level
            in (
                PerceptionGapLevel.MODERATE_DRIFT,
                PerceptionGapLevel.CRITICAL_BLINDSPOT,
            )
            and bool(
                neg_resp.stage1_physical_observation.perception_gap_explanation.strip()
            )
        )
        neg_root_ok = (
            neg_resp.stage2_learner_state.learner_archetype_id
            == "perception_blindspot"
        )
        neg_restraint_ok = (
            bool(neg_resp.stage3_teaching_decision.selected_primary_issue.strip())
            and len(neg_resp.stage3_teaching_decision.deliberately_omitted_issues)
            >= 1
            and len(neg_resp.stage3_teaching_decision.candidate_issues_detected) >= 2
        )
        neg_transfer_ok = (
            neg_resp.stage2_learner_state.transfer_status
            == BehavioralTransferStatus.PARTIAL_EXECUTION_ONLY
            and neg_resp.stage2_learner_state.conceptual_shift_verified is False
        )
        case_results.append(
            EvalCaseScore(
                case_id="negative_control_lucky_timer_attempt_2",
                archetype_id="perception_blindspot",
                attempt_number=2,
                perception_gap_passed=neg_gap_ok,
                root_cause_passed=neg_root_ok,
                single_issue_restraint_passed=neg_restraint_ok,
                sensory_grounding_score=score_sensory_grounding_with_penalty(
                    neg_resp.stage4_sensory_coaching.coaching_message
                ),
                transfer_tracking_passed=neg_transfer_ok,
                summary_note=(
                    f"Model: {neg_resp.reasoning_model_used} | "
                    f"Archetype: {neg_resp.stage2_learner_state.learner_archetype_id} | "
                    f"Transfer: {neg_resp.stage2_learner_state.transfer_status.value}"
                ),
            )
        )

        total = len(case_results)
        gap_avg = sum(1.0 if c.perception_gap_passed else 0.0 for c in case_results) / total
        root_avg = sum(1.0 if c.root_cause_passed else 0.0 for c in case_results) / total
        restraint_avg = (
            sum(1.0 if c.single_issue_restraint_passed else 0.0 for c in case_results)
            / total
        )
        sensory_avg = sum(c.sensory_grounding_score for c in case_results) / total
        transfer_avg = (
            sum(1.0 if c.transfer_tracking_passed else 0.0 for c in case_results)
            / total
        )
        overall = (
            gap_avg + root_avg + restraint_avg + sensory_avg + transfer_avg
        ) / 5.0

        return EvalSuiteReport(
            total_sessions_evaluated=total,
            perception_gap_detection_score=round(gap_avg, 3),
            latent_root_cause_accuracy=round(root_avg, 3),
            single_issue_restraint_score=round(restraint_avg, 3),
            sensory_grounding_score=round(sensory_avg, 3),
            cross_attempt_transfer_score=round(transfer_avg, 3),
            overall_human_learning_score=round(overall, 3),
            case_results=case_results,
        )
