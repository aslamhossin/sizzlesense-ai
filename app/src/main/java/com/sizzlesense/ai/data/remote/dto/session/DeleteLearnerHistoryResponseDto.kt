package com.sizzlesense.ai.data.remote.dto.session

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteLearnerHistoryResponseDto(
    @SerialName("learner_id")
    val learnerId: String,
    @SerialName("deleted_sessions")
    val deletedSessions: Int = 0
)
