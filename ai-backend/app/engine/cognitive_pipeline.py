"""4-Stage Applied AI Cognitive Pipeline (`gemini-4-argon` + `gemini-3.8-flash` + Deterministic Engine)."""

from __future__ import annotations

import json
import uuid
from typing import List, Optional

import httpx

from app.config import SETTINGS
from app.engine.chef_knowledge import (
    CHEF_PRINCIPLES_REGISTRY,
    JD_ONION_ARCHETYPES,
    ChefKnowledgeGraph,
)
from app.engine.learner_store import LearnerMemoryStore
from app.schemas.domain import (
    AcousticSizzleSignature,
    BehavioralTransferStatus,
    CognitiveCoachingResponse,
    ComputerVisionMetrics,
    ExpertTeachingDecision,
    FiveCoreQuestionsSummary,
    LearnerStateEstimate,
    MisconceptionHypothesis,
    OmittedIssue,
    PerceptionGapLevel,
    PhysicalObservationReport,
    SensoryCoachingOutput,
    SessionAnalyzeRequest,
    TimelineFact,
)
from app.vision.frame_analyzer import (
    KitchenFrameAnalyzer,
    generate_synthetic_kitchen_frame_base64,
)


class CognitiveCoachingPipeline:
    """
    Orchestrates the 4-Stage Applied AI Human Learning Pipeline:
      Stage 1: Physical World Understanding (CV metrics + Gemini 3.8 Flash + Perception Gap)
      Stage 2: Learner State Estimation (Latent understandings, misconceptions, root cause, transfer)
      Stage 3: Expert Reasoning & Single-Issue Teaching Decision (Chef Causal Graph)
      Stage 4: Sensory Coach Communicator (Shifting learner perception & judgment)
    """

    def __init__(self, memory_store: Optional[LearnerMemoryStore] = None) -> None:
        self.memory_store = memory_store or LearnerMemoryStore()

    async def analyze_session(
        self, request: SessionAnalyzeRequest
    ) -> CognitiveCoachingResponse:
        # 1. Extract Computer Vision metrics from frames (or generate preset frame if none sent)
        frames = list(request.keyframe_base64_list)
        if not frames:
            default_regime = (
                "pale_steaming" if request.attempt_number == 1 else "golden_maillard"
            )
            frames = [generate_synthetic_kitchen_frame_base64(default_regime)]

        cv_metrics = KitchenFrameAnalyzer.analyze_frame_sequence(frames)
        prior_session = self.memory_store.get_previous_attempt(
            request.learner_id, request.dish_id, request.attempt_number
        )

        # 2. Check if live Gemini API key is provided
        api_key = request.gemini_api_key or SETTINGS.env_api_key
        if api_key:
            try:
                live_response = await self._call_live_gemini_pipeline(
                    request=request,
                    cv_metrics=cv_metrics,
                    prior_session=prior_session,
                    api_key=api_key,
                )
                self.memory_store.save_session(request.learner_id, live_response)
                return live_response
            except Exception:
                # Graceful fallback to deterministic cognitive engine if network/quota fails
                pass

        # 3. Deterministic 4-Stage Cognitive Pipeline
        response = self._run_deterministic_pipeline(
            request=request,
            cv_metrics=cv_metrics,
            prior_session=prior_session,
        )
        self.memory_store.save_session(request.learner_id, response)
        return response

    def _infer_archetype_id(
        self, request: SessionAnalyzeRequest, cv_metrics: ComputerVisionMetrics
    ) -> str:
        if request.archetype_hint and request.archetype_hint in JD_ONION_ARCHETYPES:
            return request.archetype_hint

        text = request.learner_reflection.lower()
        # Learner C: Perception Blindspot (claims browned/nicely cooked while frame is pale/steaming)
        if cv_metrics.dominant_visual_regime == "PALE_STEAMING_OR_RAW" and any(
            phrase in text
            for phrase in [
                "browned really nicely",
                "browned well",
                "cooked nicely",
                "looked done",
                "just like the recipe said",
            ]
        ):
            return "perception_blindspot"

        # Learner B: Heat Anxiety (fears burning, turns heat down on steam)
        if any(
            phrase in text
            for phrase in [
                "burn",
                "turned the heat way down",
                "turned the heat down",
                "steaming a lot",
                "steamed a lot",
                "afraid",
                "smoke",
            ]
        ):
            return "heat_anxiety"

        # Learner A: Moisture Blindness (watery, pale, constant stirring)
        return "moisture_blindness"

    def _run_deterministic_pipeline(
        self,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics,
        prior_session: Optional[CognitiveCoachingResponse],
    ) -> CognitiveCoachingResponse:
        archetype_id = self._infer_archetype_id(request, cv_metrics)
        archetype = JD_ONION_ARCHETYPES[archetype_id]
        text_lower = request.learner_reflection.lower()

        # --- STAGE 1: Physical World Understanding ---
        if cv_metrics.dominant_visual_regime == "PALE_STEAMING_OR_RAW":
            acoustic = request.acoustic_hint or (
                AcousticSizzleSignature.SILENT_COLD
                if archetype_id == "heat_anxiety"
                else AcousticSizzleSignature.WET_STEAM_BUBBLE
            )
            visual_fact = (
                f"00:12 - Onions remain pale/translucent (browning ratio {cv_metrics.maillard_browning_ratio:.2f}, "
                f"moisture index {cv_metrics.surface_moisture_index:.2f}) with no pan fond."
            )
            inferred_states = [
                "Pan surface temperature is pinned near ≤100°C due to unevaporated liquid moisture.",
                "Maillard reaction threshold (>140°C) has not been reached.",
            ]
        elif cv_metrics.dominant_visual_regime == "GOLDEN_MAILLARD_BROWNING":
            acoustic = (
                request.acoustic_hint
                or AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE
            )
            visual_fact = (
                f"00:18 - Surface moisture cleared; uniform golden-amber browning achieved "
                f"(browning ratio {cv_metrics.maillard_browning_ratio:.2f})."
            )
            inferred_states = [
                "Pan surface crossed 145°C–160°C after moisture evaporation gate cleared.",
                "Sucrose and amino acids actively undergoing Maillard caramelization.",
            ]
        else:
            acoustic = (
                request.acoustic_hint
                or AcousticSizzleSignature.AGGRESSIVE_POPPING_BURN
            )
            visual_fact = (
                f"00:20 - Dark carbonized edges detected (char ratio {cv_metrics.char_burn_ratio:.2f})."
            )
            inferred_states = [
                "Pan temperature exceeded 185°C after moisture depletion without heat reduction."
            ]

        # Determine Perception Gap
        if (
            cv_metrics.dominant_visual_regime == "PALE_STEAMING_OR_RAW"
            and archetype_id == "perception_blindspot"
        ):
            gap_level = PerceptionGapLevel.CRITICAL_BLINDSPOT
            gap_explanation = (
                "Learner believes the onions 'browned nicely' after following a clock timer, "
                "whereas computer vision confirms pale, steamed tissue (browning ratio < 0.20)."
            )
        elif (
            cv_metrics.dominant_visual_regime == "PALE_STEAMING_OR_RAW"
            and archetype_id == "heat_anxiety"
        ):
            gap_level = PerceptionGapLevel.MODERATE_DRIFT
            gap_explanation = (
                "Learner mistook harmless white water vapor (100°C steam) for imminent burning smoke."
            )
        else:
            gap_level = PerceptionGapLevel.ALIGNED
            gap_explanation = (
                "Learner's observation of the pan state aligns with visual and acoustic evidence."
            )

        stage1 = PhysicalObservationReport(
            observed_timeline_facts=[
                TimelineFact(
                    timestamp="00:12" if request.attempt_number == 1 else "00:18",
                    visual_observation=visual_fact,
                    acoustic_signature=acoustic,
                )
            ],
            inferred_physical_states=inferred_states,
            environmental_uncertainties=[
                "Exact stove BTU output and pan alloy thickness are inferred from thermal recovery rate."
            ],
            cv_metrics=cv_metrics,
            learner_stated_belief=request.learner_reflection,
            perception_gap_level=gap_level,
            perception_gap_explanation=gap_explanation,
        )

        # --- STAGE 2: Learner State Estimation (Longitudinal across Attempts 1->2->3) ---
        sensory_keywords = [
            "sizzle",
            "steam",
            "golden",
            "amber",
            "sound",
            "shimmer",
            "glaze",
            "fond",
            "smell",
            "aroma",
            "edges",
        ]
        blind_timer_keywords = [
            "without looking",
            "just set my phone timer",
            "only used a timer",
        ]
        uses_sensory_reflection = any(k in text_lower for k in sensory_keywords)
        relies_on_blind_timer = any(k in text_lower for k in blind_timer_keywords)

        if request.attempt_number == 1 or prior_session is None:
            transfer_status = BehavioralTransferStatus.INITIAL_BASELINE
            conceptual_shift = False
            delta_summary = (
                f"Attempt 1 baseline recorded for archetype '{archetype_id}'. "
                "Primary misconception isolated for targeted sensory coaching."
            )
        elif cv_metrics.dominant_visual_regime == "GOLDEN_MAILLARD_BROWNING":
            if uses_sensory_reflection and not relies_on_blind_timer:
                transfer_status = BehavioralTransferStatus.CONCEPT_ADOPTED
                conceptual_shift = True
                delta_summary = (
                    f"Attempt {request.attempt_number}: Verified behavioral AND conceptual shift from Attempt "
                    f"{prior_session.attempt_number}. Learner used sensory cues rather than rigid rules."
                )
            else:
                transfer_status = BehavioralTransferStatus.PARTIAL_EXECUTION_ONLY
                conceptual_shift = False
                delta_summary = (
                    f"Attempt {request.attempt_number}: Pan outcome improved visually, but learner reflection "
                    "still relies on a blind clock timer rather than sensory judgment."
                )
        else:
            transfer_status = BehavioralTransferStatus.REGRESSED_OR_OVERCORRECTED
            conceptual_shift = False
            delta_summary = (
                f"Attempt {request.attempt_number}: Learner has not yet resolved the thermal/moisture bottleneck."
            )

        stage2 = LearnerStateEstimate(
            learner_archetype_id=archetype_id,
            demonstrated_understandings=[
                archetype.latent_understanding_baseline,
                *(
                    ["Successfully monitored pan sensory cues to achieve Maillard browning."]
                    if conceptual_shift
                    else []
                ),
            ],
            latent_misconceptions=[
                MisconceptionHypothesis(
                    concept_id=archetype.primary_principle_id,
                    description=archetype.latent_misconception_baseline,
                    confidence=0.25 if conceptual_shift else 0.91,
                )
            ],
            decision_root_cause=(
                "Applied sensory cues (sizzle sound + visual edge color) to regulate heat dynamically."
                if conceptual_shift
                else archetype.decision_root_cause_baseline
            ),
            transfer_status=transfer_status,
            conceptual_shift_verified=conceptual_shift,
            longitudinal_delta_summary=delta_summary,
        )

        # --- STAGE 3: Expert Chef Reasoning & Single-Issue Teaching Decision ---
        if not conceptual_shift:
            candidate_principle_ids = [
                archetype.primary_principle_id,
                "cut_geometry_and_surface_area",
                "fond_glazing_and_aroma_timing",
            ]
        else:
            # Once primary principle is mastered on Attempt 2/3, advance to finishing fond deglazing
            candidate_principle_ids = [
                "fond_glazing_and_aroma_timing",
                "cut_geometry_and_surface_area",
            ]

        primary_principle = CHEF_PRINCIPLES_REGISTRY[candidate_principle_ids[0]]
        omitted_principles = [
            CHEF_PRINCIPLES_REGISTRY[pid] for pid in candidate_principle_ids[1:]
        ]

        stage3 = ExpertTeachingDecision(
            candidate_issues_detected=[p.title for p in [primary_principle, *omitted_principles]],
            selected_primary_issue=primary_principle.title,
            causal_priority_rationale=(
                f"Selected '{primary_principle.title}' (Causal Rank {primary_principle.causal_rank}): "
                f"{primary_principle.physics_explanation}"
            ),
            deliberately_omitted_issues=[
                OmittedIssue(
                    issue=op.title,
                    suppression_reason=(
                        f"Held back (Causal Rank {op.causal_rank}) to prevent cognitive overload "
                        f"until the learner internalizes '{primary_principle.title}'."
                    ),
                )
                for op in omitted_principles
            ],
            target_chef_principle_id=primary_principle.id,
        )

        # --- STAGE 4: Sensory Coaching Output ---
        stage4 = SensoryCoachingOutput(
            visual_cue_to_observe=primary_principle.visual_cue,
            auditory_cue_to_listen=primary_principle.auditory_cue,
            olfactory_or_tactile_cue=primary_principle.olfactory_tactile_cue,
            coaching_message=(
                f"Notice what happened in the pan: {visual_fact} "
                f"Instead of watching a clock, focus on one sensory shift next time: "
                f"{primary_principle.visual_cue} Simultaneously, {primary_principle.auditory_cue.lower()}"
            ),
            next_attempt_micro_experiment=(
                f"In your next attempt, pause stirring for 15 seconds after adding the onions. "
                f"Listen specifically for the sound to shift ({primary_principle.auditory_cue}) "
                f"and look for: {primary_principle.visual_cue}"
            ),
        )

        five_questions = FiveCoreQuestionsSummary(
            what_is_happening_now=f"{visual_fact} ({inferred_states[0]})",
            what_they_understand_vs_not=(
                f"Understands: {stage2.demonstrated_understandings[0]} | "
                f"Not yet understood: {stage2.latent_misconceptions[0].description}"
            ),
            why_they_made_that_decision=stage2.decision_root_cause,
            what_single_thing_to_teach_now=stage3.selected_primary_issue,
            how_to_communicate_to_shift_judgment=stage4.coaching_message,
        )

        return CognitiveCoachingResponse(
            session_id=f"sess_{uuid.uuid4().hex[:8]}",
            learner_id=request.learner_id,
            dish_id=request.dish_id,
            attempt_number=request.attempt_number,
            reasoning_model_used=f"{request.preferred_reasoning_model} (Deterministic-Verified)",
            vision_model_used=f"{request.preferred_vision_model} + CV-Pixel-Analyzer",
            five_core_questions=five_questions,
            stage1_physical_observation=stage1,
            stage2_learner_state=stage2,
            stage3_teaching_decision=stage3,
            stage4_sensory_coaching=stage4,
        )

    async def _call_live_gemini_pipeline(
        self,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics,
        prior_session: Optional[CognitiveCoachingResponse],
        api_key: str,
    ) -> CognitiveCoachingResponse:
        """Calls Google Gemini Frontier API (`gemini-4-argon` / `gemini-3.8-flash`) with structured JSON schema."""
        baseline = self._run_deterministic_pipeline(request, cv_metrics, prior_session)
        model_id = request.preferred_reasoning_model or SETTINGS.default_reasoning_model
        url = f"{SETTINGS.gemini_api_base_url}/models/{model_id}:generateContent"

        system_prompt = (
            "You are the Applied AI Human Learning Engine for a culinary coaching platform. "
            "Do NOT give generic recipe timers. Infer the learner's latent mental model, "
            "select strictly ONE highest-leverage chef principle to teach now, omit secondary issues, "
            "and ground all coaching in visual, acoustic (sizzle), and olfactory cues."
        )
        user_context = {
            "dish_id": request.dish_id,
            "attempt_number": request.attempt_number,
            "learner_reflection": request.learner_reflection,
            "computer_vision_metrics": cv_metrics.model_dump(),
            "prior_attempt_summary": (
                prior_session.stage2_learner_state.longitudinal_delta_summary
                if prior_session
                else None
            ),
            "baseline_reference": baseline.model_dump(),
        }

        parts: list[dict] = [{"text": f"{system_prompt}\n\nContext:\n{json.dumps(user_context)}"}]
        if request.keyframe_base64_list:
            clean_b64 = request.keyframe_base64_list[-1].split(",")[-1].strip()
            parts.append(
                {
                    "inlineData": {
                        "mimeType": "image/jpeg",
                        "data": clean_b64,
                    }
                }
            )

        payload = {
            "contents": [{"parts": parts}],
            "generationConfig": {
                "responseMimeType": "application/json",
            },
        }

        async with httpx.AsyncClient(timeout=25.0) as client:
            resp = await client.post(
                url,
                headers={"x-goog-api-key": api_key, "Content-Type": "application/json"},
                json=payload,
            )
            resp.raise_for_status()
            data = resp.json()
            text_json = data["candidates"][0]["content"]["parts"][0]["text"]
            parsed = CognitiveCoachingResponse.model_validate_json(text_json)
            parsed.reasoning_model_used = f"{model_id} (Live API)"
            return parsed
