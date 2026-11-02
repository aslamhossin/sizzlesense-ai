"""4-Stage Applied AI Cognitive Pipeline (`gemini-3-flash-preview` + Deterministic Engine)."""

from __future__ import annotations

import json
import os
import uuid
from typing import Optional

import httpx

from app.config import SETTINGS
from app.engine.chef_knowledge import (
    CHEF_PRINCIPLES_REGISTRY,
    ONION_LEARNER_ARCHETYPES,
    ChefPrinciple,
    LearnerArchetypeScenario,
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

SENSORY_KEYWORDS: tuple[str, ...] = (
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
)

BLIND_TIMER_KEYWORDS: tuple[str, ...] = (
    "without looking",
    "just set my phone timer",
    "only used a timer",
)


class CognitiveCoachingPipeline:
    """
    Orchestrates the 4-Stage Applied AI Human Learning Pipeline following SOLID & SRP:
      - Stage 1 (`_build_stage1_physical_observation`): Physical World Understanding & Perception Gap
      - Stage 2 (`_build_stage2_learner_state`): Learner Latent State & Longitudinal Transfer
      - Stage 3 (`_build_stage3_teaching_decision`): Expert Causal Priority & Pedagogical Restraint
      - Stage 4 (`_build_stage4_sensory_coaching`): Sensory-Grounded Coaching Communication
      - Synthesis (`_synthesize_five_core_questions`): Direct mapping to the 5 Core Pedagogical Questions
    """

    def __init__(self, memory_store: Optional[LearnerMemoryStore] = None) -> None:
        self.memory_store = memory_store or LearnerMemoryStore()

    async def analyze_session(
        self, request: SessionAnalyzeRequest
    ) -> CognitiveCoachingResponse:
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

        api_key = self._resolve_active_api_key(request)
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

        response = self._run_deterministic_pipeline(
            request=request,
            cv_metrics=cv_metrics,
            prior_session=prior_session,
        )
        self.memory_store.save_session(request.learner_id, response)
        return response

    @staticmethod
    def _resolve_active_api_key(request: SessionAnalyzeRequest) -> Optional[str]:
        """Resolves valid Gemini API key while ignoring Swagger UI placeholder strings."""
        req_key = (
            request.gemini_api_key
            if request.gemini_api_key
            and request.gemini_api_key.strip().lower() != "string"
            else None
        )
        in_pytest = bool(os.environ.get("PYTEST_CURRENT_TEST"))
        return req_key or (None if in_pytest else SETTINGS.env_api_key)

    def _infer_archetype_id(
        self, request: SessionAnalyzeRequest, cv_metrics: ComputerVisionMetrics
    ) -> str:
        if request.archetype_hint and request.archetype_hint in ONION_LEARNER_ARCHETYPES:
            return request.archetype_hint

        text = request.learner_reflection.lower()
        if cv_metrics.dominant_visual_regime == "PALE_STEAMING_OR_RAW" and any(
            phrase in text
            for phrase in (
                "browned really nicely",
                "browned well",
                "cooked nicely",
                "looked done",
                "just like the recipe said",
            )
        ):
            return "perception_blindspot"

        if any(
            phrase in text
            for phrase in (
                "burn",
                "turned the heat way down",
                "turned the heat down",
                "steaming a lot",
                "steamed a lot",
                "afraid",
                "smoke",
            )
        ):
            return "heat_anxiety"

        return "moisture_blindness"

    def _run_deterministic_pipeline(
        self,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics,
        prior_session: Optional[CognitiveCoachingResponse],
    ) -> CognitiveCoachingResponse:
        archetype_id = self._infer_archetype_id(request, cv_metrics)
        archetype = ONION_LEARNER_ARCHETYPES[archetype_id]

        stage1, visual_fact, inferred_states = self._build_stage1_physical_observation(
            request=request,
            cv_metrics=cv_metrics,
            archetype_id=archetype_id,
        )
        stage2 = self._build_stage2_learner_state(
            request=request,
            cv_metrics=cv_metrics,
            archetype_id=archetype_id,
            archetype=archetype,
            prior_session=prior_session,
        )
        stage3, primary_principle = self._build_stage3_teaching_decision(
            archetype=archetype,
            conceptual_shift=stage2.conceptual_shift_verified,
        )
        stage4 = self._build_stage4_sensory_coaching(
            primary_principle=primary_principle,
            visual_fact=visual_fact,
        )
        five_questions = self._synthesize_five_core_questions(
            visual_fact=visual_fact,
            primary_inferred_state=inferred_states[0],
            stage2=stage2,
            stage3=stage3,
            stage4=stage4,
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

    def _build_stage1_physical_observation(
        self,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics,
        archetype_id: str,
    ) -> tuple[PhysicalObservationReport, str, list[str]]:
        """SRP Stage 1:Separates observed visual/acoustic facts from inferred physical pan states."""
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
        elif cv_metrics.dominant_visual_regime == "TRANSITIONAL_SAUTE":
            acoustic = (
                request.acoustic_hint
                or AcousticSizzleSignature.WET_STEAM_BUBBLE
            )
            visual_fact = (
                f"00:15 - Ambient or transitional pan state observed (luminance {cv_metrics.mean_luminance:.1f}, "
                f"browning ratio {cv_metrics.maillard_browning_ratio:.2f}, moisture index {cv_metrics.surface_moisture_index:.2f})."
            )
            inferred_states = [
                "Surface has not yet entered active Maillard browning (>140°C); awaiting thermal build-up or moisture clearing."
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

        report = PhysicalObservationReport(
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
        return report, visual_fact, inferred_states

    def _build_stage2_learner_state(
        self,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics,
        archetype_id: str,
        archetype: LearnerArchetypeScenario,
        prior_session: Optional[CognitiveCoachingResponse],
    ) -> LearnerStateEstimate:
        """SRP Stage 2: Infers latent misconceptions, root cause, and longitudinal conceptual transfer."""
        text_lower = request.learner_reflection.lower()
        uses_sensory_reflection = any(k in text_lower for k in SENSORY_KEYWORDS)
        relies_on_blind_timer = any(k in text_lower for k in BLIND_TIMER_KEYWORDS)

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

        return LearnerStateEstimate(
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

    def _build_stage3_teaching_decision(
        self,
        archetype: LearnerArchetypeScenario,
        conceptual_shift: bool,
    ) -> tuple[ExpertTeachingDecision, ChefPrinciple]:
        """SRP Stage 3: Selects the single highest-leverage causal bottleneck and suppresses secondary issues."""
        if not conceptual_shift:
            candidate_principle_ids = [
                archetype.primary_principle_id,
                "cut_geometry_and_surface_area",
                "fond_glazing_and_aroma_timing",
            ]
        else:
            candidate_principle_ids = [
                "fond_glazing_and_aroma_timing",
                "cut_geometry_and_surface_area",
            ]

        primary_principle = CHEF_PRINCIPLES_REGISTRY[candidate_principle_ids[0]]
        omitted_principles = [
            CHEF_PRINCIPLES_REGISTRY[pid] for pid in candidate_principle_ids[1:]
        ]

        decision = ExpertTeachingDecision(
            candidate_issues_detected=[
                p.title for p in [primary_principle, *omitted_principles]
            ],
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
        return decision, primary_principle

    def _build_stage4_sensory_coaching(
        self,
        primary_principle: ChefPrinciple,
        visual_fact: str,
    ) -> SensoryCoachingOutput:
        """SRP Stage 4: Generates sensory-anchored coaching and next-attempt micro-experiment."""
        return SensoryCoachingOutput(
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

    @staticmethod
    def _synthesize_five_core_questions(
        visual_fact: str,
        primary_inferred_state: str,
        stage2: LearnerStateEstimate,
        stage3: ExpertTeachingDecision,
        stage4: SensoryCoachingOutput,
    ) -> FiveCoreQuestionsSummary:
        """Maps the 4 stages directly onto the 5 Core Human Learning questions."""
        return FiveCoreQuestionsSummary(
            what_is_happening_now=f"{visual_fact} ({primary_inferred_state})",
            what_they_understand_vs_not=(
                f"Understands: {stage2.demonstrated_understandings[0]} | "
                f"Not yet understood: {stage2.latent_misconceptions[0].description}"
            ),
            why_they_made_that_decision=stage2.decision_root_cause,
            what_single_thing_to_teach_now=stage3.selected_primary_issue,
            how_to_communicate_to_shift_judgment=stage4.coaching_message,
        )

    async def _call_live_gemini_pipeline(
        self,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics,
        prior_session: Optional[CognitiveCoachingResponse],
        api_key: str,
    ) -> CognitiveCoachingResponse:
        """Calls Google Gemini API with the live camera frame and structured JSON output."""
        baseline = self._run_deterministic_pipeline(request, cv_metrics, prior_session)
        preferred = request.preferred_reasoning_model or SETTINGS.default_reasoning_model
        candidate_models = list(
            dict.fromkeys(
                [
                    "gemini-3-flash-preview",
                    "gemini-3.1-pro-preview",
                    preferred,
                    "gemini-2.5-pro",
                    "gemini-2.5-flash",
                ]
            )
        )

        system_prompt = (
            "You are the Applied AI Human Learning Engine for SizzleSense AI. "
            "Inspect the attached kitchen camera image and the learner's reflection. "
            "Return ONLY a single valid JSON object with the EXACT top-level keys of the JSON template below "
            "(`session_id`, `learner_id`, `dish_id`, `attempt_number`, `reasoning_model_used`, `vision_model_used`, "
            "`five_core_questions`, `stage1_physical_observation`, `stage2_learner_state`, `stage3_teaching_decision`, "
            "`stage4_sensory_coaching`). Do NOT wrap it in an outer key. "
            "Update `observed_timeline_facts[0].visual_observation`, `inferred_physical_states`, "
            "`five_core_questions`, and `stage4_sensory_coaching.coaching_message` based on what you actually "
            "see in the attached camera image and learner reflection, while keeping at least 1 item in `deliberately_omitted_issues`."
        )

        parts: list[dict] = [
            {
                "text": (
                    f"{system_prompt}\n\n"
                    f"Learner Reflection: {request.learner_reflection}\n"
                    f"Computer Vision Metrics: {json.dumps(cv_metrics.model_dump())}\n"
                    f"JSON Template to populate and return:\n{json.dumps(baseline.model_dump())}"
                )
            }
        ]
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

        last_err: Exception | None = None
        async with httpx.AsyncClient(timeout=30.0) as client:
            for model_id in candidate_models:
                url = f"{SETTINGS.gemini_api_base_url}/models/{model_id}:generateContent"
                try:
                    resp = await client.post(
                        url,
                        headers={
                            "x-goog-api-key": api_key,
                            "Content-Type": "application/json",
                        },
                        json=payload,
                    )
                    if resp.status_code != 200:
                        continue
                    data = resp.json()
                    text_json = data["candidates"][0]["content"]["parts"][0]["text"]
                    raw_obj = json.loads(text_json)
                    if isinstance(raw_obj, dict) and "baseline_reference" in raw_obj:
                        raw_obj = raw_obj["baseline_reference"]
                    parsed = CognitiveCoachingResponse.model_validate(raw_obj)
                    parsed.reasoning_model_used = f"{model_id} (Live Gemini API)"
                    return parsed
                except Exception as exc:
                    last_err = exc
                    continue

        if last_err:
            raise last_err
        raise RuntimeError("No Gemini model endpoint succeeded.")
