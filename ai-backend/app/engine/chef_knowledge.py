"""Chef Causal Knowledge Graph & Sautéed Onion Learner Archetypes."""

from __future__ import annotations

from typing import Dict, List, Tuple
from pydantic import BaseModel, Field

from app.schemas.domain import AcousticSizzleSignature, PerceptionGapLevel


class ChefPrinciple(BaseModel):
    """A reusable culinary principle derived from world-class chefs' tacit knowledge."""

    id: str
    title: str
    causal_rank: int = Field(
        ...,
        ge=1,
        le=10,
        description="1 = Highest causal prerequisite (must be solved first); higher numbers = downstream refinements",
    )
    physics_explanation: str
    visual_cue: str
    auditory_cue: str
    olfactory_tactile_cue: str
    applicable_dishes: List[str]


class AttemptScenarioPreset(BaseModel):
    """A single attempt scenario (Attempt 1, 2, or 3) for a specific learner archetype."""

    attempt_number: int = Field(..., ge=1, le=3)
    synthetic_frame_regime: str
    acoustic_signature: AcousticSizzleSignature
    learner_reflection: str
    observed_visual_fact: str
    expected_perception_gap: PerceptionGapLevel


class LearnerArchetypeScenario(BaseModel):
    """Models one of the 3 Sautéed Onion learner archetypes across 3 iterative attempts."""

    archetype_id: str
    display_name: str
    pedagogical_scenario_summary: str
    primary_principle_id: str
    latent_understanding_baseline: str
    latent_misconception_baseline: str
    decision_root_cause_baseline: str
    attempts: Dict[int, AttemptScenarioPreset]


CHEF_PRINCIPLES_REGISTRY: Dict[str, ChefPrinciple] = {
    "moisture_evaporation_gate": ChefPrinciple(
        id="moisture_evaporation_gate",
        title="Surface Moisture Evaporation Gate (100°C Boiling vs. 140°C Browning)",
        causal_rank=1,
        physics_explanation=(
            "Liquid water pins pan surface temperature at 100°C due to high latent heat of vaporization. "
            "Maillard browning reactions cannot start until surface water evaporates and temperature crosses ~140°C."
        ),
        visual_cue="Watch milky/translucent liquid pools shrink into clear, shimmering oil droplets around onion edges.",
        auditory_cue="Listen for the transition from low-pitched wet bubbling (boiling) to a crisp, high-frequency applause sizzle.",
        olfactory_tactile_cue="Raw pungent sulfur steam shifts to sweet, nutty roasted aroma once water clears.",
        applicable_dishes=["sauteed_onions", "crispy_chicken_thigh", "pan_seared_mushrooms"],
    ),
    "thermal_recovery_and_heat_control": ChefPrinciple(
        id="thermal_recovery_and_heat_control",
        title="Pan Thermal Recovery & Managing Heat Anxiety (Steam != Smoke)",
        causal_rank=2,
        physics_explanation=(
            "Adding cold ingredients drops pan temperature sharply. Mistaking harmless white water vapor (steam) "
            "for burning smoke leads learners to turn the heat down too far, trapping food in a low-heat sweat."
        ),
        visual_cue="Distinguish wispy white water vapor (harmless steam that dissipates quickly) from thin bluish oil smoke.",
        auditory_cue="If the pan goes quiet within 10 seconds of adding onions, heat is too low to recover thermal momentum.",
        olfactory_tactile_cue="Hold palm 15cm above pan to feel steady radiant warmth; pause stirring for 15–20s so contact heat transfers.",
        applicable_dishes=["sauteed_onions", "pan_seared_steak", "stir_fry_vegetables"],
    ),
    "visual_and_acoustic_doneness_calibration": ChefPrinciple(
        id="visual_and_acoustic_doneness_calibration",
        title="Sensory Doneness Calibration (Translucent Sweat vs. Golden Caramelization)",
        causal_rank=3,
        physics_explanation=(
            "Softening (pectin breakdown at ~85°C) happens before browning (Maillard reaction at >140°C). "
            "Judging doneness by softness or elapsed clock time alone causes premature stopping."
        ),
        visual_cue="Look for golden-amber edges and a thin bronze glaze (fond) forming on the stainless pan floor.",
        auditory_cue="Sizzle softens slightly as internal moisture drops, requiring smaller splashes of water/oil to deglaze.",
        olfactory_tactile_cue="Tasting a piece reveals deep savory sweetness rather than floppy boiled sharpness.",
        applicable_dishes=["sauteed_onions", "french_omelette", "caramelized_shallots"],
    ),
    "cut_geometry_and_surface_area": ChefPrinciple(
        id="cut_geometry_and_surface_area",
        title="Uniform Cut Geometry & Pan Contact Surface",
        causal_rank=4,
        physics_explanation=(
            "Varying piece thickness causes thin slivers to carbonize while thick wedges remain raw inside."
        ),
        visual_cue="Check that sliced pieces lie in an even single layer with at least 20% visible pan floor between pieces.",
        auditory_cue="Even contact produces a uniform sizzle across the skillet rather than isolated loud spots.",
        olfactory_tactile_cue="Pieces yield with uniform resistance against the spatula edge.",
        applicable_dishes=["sauteed_onions", "roasted_root_vegetables"],
    ),
    "fond_glazing_and_aroma_timing": ChefPrinciple(
        id="fond_glazing_and_aroma_timing",
        title="Fond Deglazing & Finishing Aroma Timing",
        causal_rank=5,
        physics_explanation=(
            "Browned proteins and sugars stuck to the pan floor (fond) dissolve into a glossy emulsion when a small splash of liquid is added."
        ),
        visual_cue="Watch the amber crust on the pan floor dissolve immediately into a glossy coating on the onions.",
        auditory_cue="A sharp, brief hiss when deglazing liquid hits the hot pan floor.",
        olfactory_tactile_cue="Rich umami aroma blooms immediately upon deglazing.",
        applicable_dishes=["sauteed_onions", "pan_seared_steak", "crispy_chicken_thigh"],
    ),
}


ONION_LEARNER_ARCHETYPES: Dict[str, LearnerArchetypeScenario] = {
    "moisture_blindness": LearnerArchetypeScenario(
        archetype_id="moisture_blindness",
        display_name="Learner A: Moisture Blindness (Crowds pan & stirs constantly in liquid)",
        pedagogical_scenario_summary="One may not understand why moisture needs to be cooked off.",
        primary_principle_id="moisture_evaporation_gate",
        latent_understanding_baseline="Understands basic ingredient prep and medium burner setting.",
        latent_misconception_baseline=(
            "Does not understand that pooled liquid water locks pan temperature at 100°C and physically blocks browning."
        ),
        decision_root_cause_baseline=(
            "Piled a thick heap of onions into the pan at once and stirred continuously, trapping steam and pooling water."
        ),
        attempts={
            1: AttemptScenarioPreset(
                attempt_number=1,
                synthetic_frame_regime="pale_steaming",
                acoustic_signature=AcousticSizzleSignature.WET_STEAM_BUBBLE,
                learner_reflection=(
                    "I kept stirring the onions for 8 minutes on medium heat, but they stayed pale and watery instead of browning."
                ),
                observed_visual_fact="00:12 - Onions are pale white, stacked 3 layers deep, simmering in 4mm of cloudy liquid.",
                expected_perception_gap=PerceptionGapLevel.ALIGNED,
            ),
            2: AttemptScenarioPreset(
                attempt_number=2,
                synthetic_frame_regime="golden_maillard",
                acoustic_signature=AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE,
                learner_reflection=(
                    "I spread the onions out and waited until the wet bubbling sound turned into a crisp sizzle before stirring!"
                ),
                observed_visual_fact="00:18 - Liquid pool evaporated; clear oil shimmer and golden-amber edges visible on onions.",
                expected_perception_gap=PerceptionGapLevel.ALIGNED,
            ),
            3: AttemptScenarioPreset(
                attempt_number=3,
                synthetic_frame_regime="golden_maillard",
                acoustic_signature=AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE,
                learner_reflection=(
                    "As soon as I heard the sizzle sharpen after moisture cooked off, I adjusted heat to keep steady golden browning without burning."
                ),
                observed_visual_fact="00:24 - Deep, even amber Maillard browning across the skillet with clean fond.",
                expected_perception_gap=PerceptionGapLevel.ALIGNED,
            ),
        },
    ),
    "heat_anxiety": LearnerArchetypeScenario(
        archetype_id="heat_anxiety",
        display_name="Learner B: Heat Anxiety (Fears burning & turns heat down too low)",
        pedagogical_scenario_summary="Another may be afraid of burning the onions and may have turned the heat down too far.",
        primary_principle_id="thermal_recovery_and_heat_control",
        latent_understanding_baseline="Understands that burnt onions taste bitter and watches the pan closely.",
        latent_misconception_baseline=(
            "Confuses harmless white water vapor (steam release) with burning smoke, believing any loud sizzle means imminent scorching."
        ),
        decision_root_cause_baseline=(
            "Panicked when initial steam rose at 00:05 and immediately dialed the burner down to minimum, stalling thermal recovery."
        ),
        attempts={
            1: AttemptScenarioPreset(
                attempt_number=1,
                synthetic_frame_regime="pale_steaming",
                acoustic_signature=AcousticSizzleSignature.SILENT_COLD,
                learner_reflection=(
                    "It started steaming a lot right away so I turned the heat way down so it wouldn't burn, and then nothing really browned."
                ),
                observed_visual_fact="00:10 - White water vapor rose briefly, then pan went completely silent with limp, pale onions.",
                expected_perception_gap=PerceptionGapLevel.MODERATE_DRIFT,
            ),
            2: AttemptScenarioPreset(
                attempt_number=2,
                synthetic_frame_regime="golden_maillard",
                acoustic_signature=AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE,
                learner_reflection=(
                    "I saw the white steam again, recognized it was just water escaping, and kept the heat steady until the edges turned gold."
                ),
                observed_visual_fact="00:16 - Steady white steam cleared without blue smoke; onions developed warm golden edges.",
                expected_perception_gap=PerceptionGapLevel.ALIGNED,
            ),
            3: AttemptScenarioPreset(
                attempt_number=3,
                synthetic_frame_regime="golden_maillard",
                acoustic_signature=AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE,
                learner_reflection=(
                    "I used the steady applause sizzle sound and clear oil shimmer to ride medium-high heat confidently, then eased down once golden."
                ),
                observed_visual_fact="00:22 - Uniform golden-brown onions with controlled thermal momentum and zero scorching.",
                expected_perception_gap=PerceptionGapLevel.ALIGNED,
            ),
        },
    ),
    "perception_blindspot": LearnerArchetypeScenario(
        archetype_id="perception_blindspot",
        display_name="Learner C: Perception Blindspot (Believes translucent onions are browned)",
        pedagogical_scenario_summary="Or the learner may believe they cooked the onions sufficiently even though the video shows otherwise.",
        primary_principle_id="visual_and_acoustic_doneness_calibration",
        latent_understanding_baseline="Follows recipe steps sequentially and stirs diligently.",
        latent_misconception_baseline=(
            "Equates 'soft and translucent after 5 minutes on a clock' with 'sufficiently sautéed and browned'."
        ),
        decision_root_cause_baseline=(
            "Stopped cooking as soon as the 5-minute recipe timer rang and the onions looked soft, without checking pan fond or amber color."
        ),
        attempts={
            1: AttemptScenarioPreset(
                attempt_number=1,
                synthetic_frame_regime="pale_steaming",
                acoustic_signature=AcousticSizzleSignature.WET_STEAM_BUBBLE,
                learner_reflection=(
                    "I sautéed the onions for 5 minutes just like the recipe said and they browned really nicely!"
                ),
                observed_visual_fact="00:14 - Onions are pale white/translucent with zero Maillard browning or pan fond.",
                expected_perception_gap=PerceptionGapLevel.CRITICAL_BLINDSPOT,
            ),
            2: AttemptScenarioPreset(
                attempt_number=2,
                synthetic_frame_regime="golden_maillard",
                acoustic_signature=AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE,
                learner_reflection=(
                    "I ignored the 5-minute timer this time and waited until I saw actual amber-gold color on the edges and a light brown glaze on the pan."
                ),
                observed_visual_fact="00:19 - Distinct transition from translucent white to amber-gold edges with visible pan fond.",
                expected_perception_gap=PerceptionGapLevel.ALIGNED,
            ),
            3: AttemptScenarioPreset(
                attempt_number=3,
                synthetic_frame_regime="golden_maillard",
                acoustic_signature=AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE,
                learner_reflection=(
                    "Now I can immediately tell the difference between sweaty translucent onions and true nutty golden browning by sight and smell."
                ),
                observed_visual_fact="00:25 - Rich caramel-amber onions with deglazed glossy fond.",
                expected_perception_gap=PerceptionGapLevel.ALIGNED,
            ),
        },
    ),
}


class ChefKnowledgeGraph:
    """Queries and prioritizes principles from the Chef Causal Knowledge Graph."""

    @staticmethod
    def select_primary_and_omitted(
        candidate_principle_ids: List[str],
    ) -> Tuple[ChefPrinciple, List[ChefPrinciple]]:
        matched = [
            CHEF_PRINCIPLES_REGISTRY[pid]
            for pid in candidate_principle_ids
            if pid in CHEF_PRINCIPLES_REGISTRY
        ]
        if not matched:
            matched = [CHEF_PRINCIPLES_REGISTRY["moisture_evaporation_gate"]]

        sorted_principles = sorted(matched, key=lambda p: p.causal_rank)
        primary = sorted_principles[0]
        omitted = sorted_principles[1:]
        return primary, omitted
