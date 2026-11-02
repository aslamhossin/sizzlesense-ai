"""Tests for Atomic Step 5: Automated Human Learning Evaluation Suite (`EvalBench`)."""

import pytest

from app.evals.human_learning_eval import (
    HumanLearningEvalRunner,
    score_sensory_grounding_with_penalty,
)


@pytest.mark.asyncio
async def test_human_learning_eval_suite_runs_all_archetypes_across_three_attempts():
    """
    Runs all 3 learner archetypes across Attempts 1 -> 2 -> 3 (9 sessions total)
    Plus 1 negative-control 'lucky timer' transfer case (10 sessions total)
    and verifies all 5 Human Learning Evaluation metrics score >= 0.90.
    """
    runner = HumanLearningEvalRunner()
    report = await runner.run_full_benchmark()

    assert report.total_sessions_evaluated == 10
    assert report.perception_gap_detection_score >= 0.95
    assert report.latent_root_cause_accuracy >= 0.95
    assert report.single_issue_restraint_score == 1.0
    assert report.sensory_grounding_score >= 0.95
    assert report.cross_attempt_transfer_score >= 0.95
    assert report.overall_human_learning_score >= 0.95
    assert len(report.case_results) == 10


def test_sensory_grounding_penalizes_rigid_timer_rules():
    """Verify the anti-pattern penalty drops the score when coaching gives rigid minute rules instead of sensory cues."""
    good_coaching = (
        "Watch the milky liquid shrink into clear oil shimmer and listen for a crisp applause sizzle."
    )
    bad_timer_coaching = (
        "Next time, cook for exactly 7 minutes on medium heat and then stop."
    )

    assert score_sensory_grounding_with_penalty(good_coaching) == 1.0
    assert score_sensory_grounding_with_penalty(bad_timer_coaching) == 0.0
