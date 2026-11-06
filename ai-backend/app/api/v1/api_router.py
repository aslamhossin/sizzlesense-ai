"""Aggregated API v1 router combining health, sessions, evals, and camera endpoints."""

from __future__ import annotations

from fastapi import APIRouter

from app.api.v1.routers import camera, evals, health, sessions

api_v1_router = APIRouter(prefix="/api/v1")
api_v1_router.include_router(health.router)
api_v1_router.include_router(sessions.router)
api_v1_router.include_router(evals.router)
api_v1_router.include_router(camera.router)
