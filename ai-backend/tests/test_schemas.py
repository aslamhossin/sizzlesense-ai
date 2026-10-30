"""Tests for SizzleSense AI strict Pydantic domain schemas (4 Pillars & 5 Core JD Questions)."""

import pytest
from pydantic import ValidationError

from app.schemas.domain import (
    AcousticSizzleSignature,
    BehavioralTransferStatus,
    CognitiveCoachingResponse,
    ExpertTeachingDecision,
    FiveCoreQuestionsSummary,
    LearnerStateEstimate,
    MisconceptionHypothesis,
    OmittedIssue,
    PerceptionGapLevel,
    PhysicalObservationReport,
    SensoryCoachingOutput,
    TimelineFact,
)


def test_physical_observation_separates_facts_from_inferred_states():
    """Pillar 1: Must explicitly separate observed facts, inferred states, and self-assessment discrepancy."""
    report = PhysicalObservationReport(
        observed_timeline_facts=[
            TimelineFact(
                timestamp="00:12",
                visual_observation="Onions are pale and translucent, pooling in 3mm of liquid.",
                acoustic_signature=AcousticSizzleSignature.WET_STEAM_BUBBLE,
            )
        ],
        inferred_physical_states=[
            "Pan surface temperature is pinned near 100°C due to latent heat of water evaporation."
        ],
        environmental_uncertainties=[
            "Exact burner BTU rating is unknown from overhead camera."
        ],
        learner_stated_belief="I browned the onions well on medium heat, though they got a bit soft.",
        perception_gap_level=PerceptionGapLevel.CRITICAL_BLINDSPOT,
        perception_gap_explanation="Learner mistakes translucent steaming for Maillard browning.",
    )
    assert report.perception_gap_level == PerceptionGapLevel.CRITICAL_BLINDSPOT
    assert len(report.observed_timeline_facts) == 1
    assert len(report.inferred_physical_states) == 1


def test_expert_teaching_decision_enforces_single_issue_restraint():
    """Pillar 3: Must select strictly 1 primary issue and log omitted secondary issues when multiple exist."""
    decision = ExpertTeachingDecision(
        candidate_issues_detected=[
            "Moisture trapping / steaming instead of sautéing",
            "Uneven knife dice size (4mm to 18mm)",
            "Late salt timing",
        ],
        selected_primary_issue="Moisture trapping / steaming instead of sautéing",
        causal_priority_rationale=(
            "Pan thermal recovery and surface moisture evaporation (Rank 1) physically gate "
            "Maillard browning regardless of dice uniformity."
        ),
        deliberately_omitted_issues=[
            OmittedIssue(
                issue="Uneven knife dice size (4mm to 18mm)",
                suppression_reason="Teaching knife work now would overload the learner before they can hear/see pan temperature.",
            ),
            OmittedIssue(
                issue="Late salt timing",
                suppression_reason="Secondary optimization compared to the primary thermal bottleneck.",
            ),
        ],
        target_chef_principle_id="moisture_evaporation_gate",
    )
    assert decision.selected_primary_issue == "Moisture trapping / steaming instead of sautéing"
    assert len(decision.deliberately_omitted_issues) == 2


def test_expert_teaching_decision_rejects_missing_omissions_when_multiple_candidates():
    """If 2+ candidate issues exist, deliberately_omitted_issues must not be empty."""
    with pytest.raises(ValidationError):
        ExpertTeachingDecision(
            candidate_issues_detected=["Issue A", "Issue B"],
            selected_primary_issue="Issue A",
            causal_priority_rationale="A comes first",
            deliberately_omitted_issues=[],
            target_chef_principle_id="moisture_evaporation_gate",
        )


def test_full_cognitive_response_answers_all_five_jd_questions():
    """Verify the top-level response directly maps to the 5 Core Questions in the Cookpad JD."""
    five_q = FiveCoreQuestionsSummary(
        what_is_happening_now="Onions are boiling in their own expelled moisture at ~100°C rather than sautéing.",
        what_they_understand_vs_not=(
            "Understands basic pan setup; does NOT yet understand that liquid water blocks Maillard browning (>140°C)."
        ),
        why_they_made_that_decision=(
            "Afraid of burning the onions after seeing initial steam, so they turned the burner down too low."
        ),
        what_single_thing_to_teach_now="Listen for the transition from wet bubbling to dry applause-like sizzle.",
        how_to_communicate_to_shift_judgment=(
            "Anchor coaching on the acoustic cue at 00:12 rather than giving a fixed minute timer."
        ),
    )
    assert "100°C" in five_q.what_is_happening_now
    assert "afraid of burning" in five_q.why_they_made_that_decision.lower()
