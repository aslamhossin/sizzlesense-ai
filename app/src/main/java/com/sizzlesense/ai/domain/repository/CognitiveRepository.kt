package com.sizzlesense.ai.domain.repository

interface CognitiveRepository :
    SessionAnalysisGateway,
    LiveTapoCameraGateway,
    EvaluationBenchmarkGateway
