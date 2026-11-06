"""Human Learning Evaluation Benchmark (`EvalBench`) router."""

from __future__ import annotations

from fastapi import APIRouter, Depends

from app.api.dependencies import get_eval_runner
from app.evals.human_learning_eval import EvalSuiteReport, HumanLearningEvalRunner

router = APIRouter(tags=["Human Learning EvalBench"])


@router.post("/evals/run", response_model=EvalSuiteReport)
async def run_human_learning_eval_suite(
    eval_runner: HumanLearningEvalRunner = Depends(get_eval_runner),
) -> EvalSuiteReport:
    """Executes the 10-session Automated Human Learning Evaluation Benchmark (`EvalBench`)."""
    return await eval_runner.run_full_benchmark()
