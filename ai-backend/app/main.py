"""FastAPI Application Factory for SizzleSense AI — Applied AI Human Learning & Culinary Perception Engine."""

from __future__ import annotations

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.v1.api_router import api_v1_router


def create_app() -> FastAPI:
    """Creates and configures the modular FastAPI application."""
    application = FastAPI(
        title="SizzleSense AI — Applied AI Human Learning Engine",
        description=(
            "4-Stage Multimodal Culinary Coaching & Human Learning Evaluation API "
            "(Physical World Understanding -> Learner Latent State -> Single-Issue Chef Selection -> Sensory Coaching)."
        ),
        version="1.0.0",
    )

    application.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    application.include_router(api_v1_router)
    return application


app = create_app()
