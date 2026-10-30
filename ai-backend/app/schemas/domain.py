"""Strict Pydantic v2 schemas for the 4 Applied AI Pillars and 5 Core JD Questions."""

from __future__ import annotations

from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field, model_validator


class AcousticSizzleSignature(str, Enum):
    """Acoustic state of the cooking vessel inferred from audio/video cues."""

    SILENT_COLD = "SILENT_COLD"
    WET_STEAM_BUBBLE = "WET_STEAM_BUBBLE"  # ~100°C boiling/steaming in expelled water
    STEADY_APPLAUSE_SIZZLE = "STEADY_APPLAUSE_SIZZLE"  # 140°C-165°C optimal Maillard zone
    AGGRESSIVE_POPPING_BURN = "AGGRESSIVE_POPPING_BURN"  # >185°C pyrolysis / scorching


class PerceptionGapLevel(str, Enum):
    """Discrepancy between what the camera/audio shows vs. what the learner believes."""

    ALIGNED = "ALIGNED"
    MODERATE_DRIFT = "MODERATE_DRIFT"
    CRITICAL_BLINDSPOT = "CRITICAL_BLINDSPOT"


class BehavioralTransferStatus(str, Enum):
    """Longitudinal skill transfer state across the 3-Attempt Mastery Loop."""

    INITIAL_BASELINE = "INITIAL_BASELINE"
    CONCEPT_ADOPTED = "CONCEPT_ADOPTED"
    PARTIAL_EXECUTION_ONLY = "PARTIAL_EXECUTION_ONLY"  # Lucky execution without conceptual shift
    REGRESSED_OR_OVERCORRECTED = "REGRESSED_OR_OVERCORRECTED"


class TimelineFact(BaseModel):
    """An objective, timestamped observation from video/audio."""

    timestamp: str = Field(..., description="MM:SS timestamp in the cooking clip")
    visual_observation: str = Field(..., description="Directly observable visual fact")
    acoustic_signature: AcousticSizzleSignature = Field(
        default=AcousticSizzleSignature.WET_STEAM_BUBBLE,
        description="Pan sound profile at this timestamp",
    )


class ComputerVisionMetrics(BaseModel):
    """Objective pixel-level metrics extracted by the Computer Vision frame analyzer."""

    maillard_browning_ratio: float = Field(..., ge=0.0, le=1.0)
    surface_moisture_index: float = Field(..., ge=0.0, le=1.0)
    char_burn_ratio: float = Field(..., ge=0.0, le=1.0)
    mean_luminance: float = Field(..., ge=0.0, le=255.0)
    dominant_visual_regime: str


class PhysicalObservationReport(BaseModel):
    """Pillar 1: Physical World Understanding (Observed Facts vs. Inferred States vs. Learner Belief)."""

    observed_timeline_facts: List[TimelineFact] = Field(min_length=1)
    inferred_physical_states: List[str] = Field(
        min_length=1,
        description="Physical states inferred from evidence (e.g., pan temperature pinned at 100°C)",
    )
    environmental_uncertainties: List[str] = Field(
        default_factory=list,
        description="Home kitchen variables that cannot be directly observed (e.g., burner BTU)",
    )
    cv_metrics: Optional[ComputerVisionMetrics] = None
    learner_stated_belief: str = Field(..., description="What the learner claimed in their reflection")
    perception_gap_level: PerceptionGapLevel
    perception_gap_explanation: str


class MisconceptionHypothesis(BaseModel):
    """A ranked hypothesis about what the learner does not yet understand."""

    concept_id: str
    description: str
    confidence: float = Field(..., ge=0.0, le=1.0)


class LearnerStateEstimate(BaseModel):
    """Pillar 2: Learner State Estimation (Latent understandings, misconceptions, and decision root cause)."""

    learner_archetype_id: str = Field(
        default="unknown",
        description="Inferred learner archetype (e.g., moisture_blindness, heat_anxiety, perception_blindspot)",
    )
    demonstrated_understandings: List[str]
    latent_misconceptions: List[MisconceptionHypothesis]
    decision_root_cause: str = Field(
        ...,
        description="Why the learner made that specific decision in the moment",
    )
    transfer_status: BehavioralTransferStatus = Field(
        default=BehavioralTransferStatus.INITIAL_BASELINE
    )
    conceptual_shift_verified: bool = Field(
        default=False,
        description="True only when BOTH physical execution and learner sensory reflection align",
    )
    longitudinal_delta_summary: str = Field(
        default="Attempt 1 baseline established.",
        description="How the learner's judgment evolved compared to previous attempts",
    )


class OmittedIssue(BaseModel):
    """An issue intentionally held back to avoid cognitive overload."""

    issue: str
    suppression_reason: str


class ExpertTeachingDecision(BaseModel):
    """Pillar 3: Expert Reasoning & Single-Issue Teaching Decision."""

    candidate_issues_detected: List[str] = Field(min_length=1)
    selected_primary_issue: str = Field(
        ..., description="The SINGLE highest-leverage issue to teach right now"
    )
    causal_priority_rationale: str = Field(
        ...,
        description="Why this issue causally precedes the other candidate issues in the Chef Knowledge Graph",
    )
    deliberately_omitted_issues: List[OmittedIssue] = Field(default_factory=list)
    target_chef_principle_id: str

    @model_validator(mode="after")
    def validate_pedagogical_restraint(self) -> "ExpertTeachingDecision":
        if (
            len(self.candidate_issues_detected) > 1
            and len(self.deliberately_omitted_issues) == 0
        ):
            raise ValueError(
                "Pedagogical Restraint Violation: When multiple candidate issues are detected, "
                "deliberately_omitted_issues must explicitly document what was suppressed and why."
            )
        return self


class SensoryCoachingOutput(BaseModel):
    """Pillar 4a: Communication designed to change how the learner perceives and judges."""

    visual_cue_to_observe: str
    auditory_cue_to_listen: str
    olfactory_or_tactile_cue: str
    coaching_message: str = Field(
        ...,
        description="Chef coaching focused on sensory judgment rather than rigid recipe rules",
    )
    next_attempt_micro_experiment: str = Field(
        ...,
        description="Concrete sensory hypothesis for the learner to test in their next cooking session",
    )


class FiveCoreQuestionsSummary(BaseModel):
    """Direct 1:1 answers to the 5 Core Questions in the Cookpad Applied AI Engineer JD."""

    what_is_happening_now: str
    what_they_understand_vs_not: str
    why_they_made_that_decision: str
    what_single_thing_to_teach_now: str
    how_to_communicate_to_shift_judgment: str


class SessionAnalyzeRequest(BaseModel):
    """Input payload from the Android MVI Client or Tapo C100 RTSP frame grabber."""

    learner_id: str = Field(default="learner_default")
    dish_id: str = Field(default="sauteed_onions")
    attempt_number: int = Field(default=1, ge=1, le=3)
    learner_reflection: str = Field(
        ..., description="Learner's post-cooking audio/text reflection"
    )
    archetype_hint: Optional[str] = Field(
        default=None,
        description="Optional preset scenario archetype (moisture_blindness, heat_anxiety, perception_blindspot)",
    )
    keyframe_base64_list: List[str] = Field(
        default_factory=list,
        description="Optional base64-encoded JPEG frames captured from Tapo C100 RTSP or gallery",
    )
    acoustic_hint: Optional[AcousticSizzleSignature] = None
    gemini_api_key: Optional[str] = None
    preferred_reasoning_model: str = Field(default="gemini-4-argon")
    preferred_vision_model: str = Field(default="gemini-3.8-flash")


class CognitiveCoachingResponse(BaseModel):
    """Full 4-stage output returned to the Android MVI app and X-Ray Inspector."""

    session_id: str
    learner_id: str
    dish_id: str
    attempt_number: int
    reasoning_model_used: str
    vision_model_used: str
    five_core_questions: FiveCoreQuestionsSummary
    stage1_physical_observation: PhysicalObservationReport
    stage2_learner_state: LearnerStateEstimate
    stage3_teaching_decision: ExpertTeachingDecision
    stage4_sensory_coaching: SensoryCoachingOutput
