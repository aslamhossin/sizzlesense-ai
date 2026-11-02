"""Tests for Atomic Step 3: Chef Causal Knowledge Graph & Sautéed Onion Learner Archetypes."""

from app.engine.chef_knowledge import (
    CHEF_PRINCIPLES_REGISTRY,
    ONION_LEARNER_ARCHETYPES,
    ChefKnowledgeGraph,
)


def test_chef_knowledge_graph_contains_cross_dish_principles():
    """Verify the knowledge graph encodes reusable culinary physics across dishes."""
    assert "moisture_evaporation_gate" in CHEF_PRINCIPLES_REGISTRY
    assert "thermal_recovery_and_heat_control" in CHEF_PRINCIPLES_REGISTRY
    assert "visual_and_acoustic_doneness_calibration" in CHEF_PRINCIPLES_REGISTRY
    assert "cut_geometry_and_surface_area" in CHEF_PRINCIPLES_REGISTRY


def test_causal_priority_selects_rank_1_and_omits_secondary_issues():
    """When multiple faults occur, the selector must pick the lowest causal_rank (highest priority) and omit the rest."""
    detected_ids = [
        "cut_geometry_and_surface_area",  # Rank 3
        "moisture_evaporation_gate",  # Rank 1 (blocks browning physically)
        "fond_glazing_and_aroma_timing",  # Rank 4
    ]
    primary, omitted = ChefKnowledgeGraph.select_primary_and_omitted(detected_ids)

    assert primary.id == "moisture_evaporation_gate"
    assert primary.causal_rank == 1
    assert len(omitted) == 2
    assert omitted[0].id == "cut_geometry_and_surface_area"
    assert omitted[1].id == "fond_glazing_and_aroma_timing"


def test_three_onion_learner_archetypes_have_distinct_latent_causes():
    """The 3 onion learner archetypes fail visually in the same way on Attempt 1, but for 3 distinct latent reasons."""
    assert set(ONION_LEARNER_ARCHETYPES.keys()) == {
        "moisture_blindness",
        "heat_anxiety",
        "perception_blindspot",
    }

    m_learner = ONION_LEARNER_ARCHETYPES["moisture_blindness"]
    h_learner = ONION_LEARNER_ARCHETYPES["heat_anxiety"]
    p_learner = ONION_LEARNER_ARCHETYPES["perception_blindspot"]

    # All 3 have 3-attempt progressions (Attempt 1 -> Attempt 2 -> Attempt 3)
    assert len(m_learner.attempts) == 3
    assert len(h_learner.attempts) == 3
    assert len(p_learner.attempts) == 3

    # Distinct root causes on Attempt 1 despite identical pale/under-sautéed visual failure
    assert m_learner.primary_principle_id != h_learner.primary_principle_id
    assert h_learner.primary_principle_id != p_learner.primary_principle_id
