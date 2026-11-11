package com.sizzlesense.ai.data.session

import android.content.Context
import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.coaching.toDomain
import com.sizzlesense.ai.data.remote.dto.coaching.toDto
import com.sizzlesense.ai.data.remote.dto.eval.EvalSuiteReportDto
import com.sizzlesense.ai.data.remote.dto.eval.toDomain
import com.sizzlesense.ai.data.remote.dto.eval.toDto
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PersistedTapoCameraConfigDto(
    @SerialName("ip_address")
    val ipAddress: String = BuildConfig.DEFAULT_TAPO_IP,
    val username: String = "",
    val password: String = "",
    @SerialName("use_sub_stream")
    val useSubStream: Boolean = false,
    val port: Int = 554,
    @SerialName("is_connected")
    val isConnected: Boolean = false
)

@Serializable
data class PersistedSessionSnapshotDto(
    @SerialName("backend_base_url")
    val backendBaseUrl: String = BuildConfig.DEFAULT_BACKEND_URL,
    @SerialName("tapo_config")
    val tapoConfig: PersistedTapoCameraConfigDto = PersistedTapoCameraConfigDto(),
    @SerialName("selected_archetype")
    val selectedArchetype: String = LearnerArchetypeOption.MOISTURE_BLINDNESS.name,
    @SerialName("current_attempt_number")
    val currentAttemptNumber: Int = 1,
    @SerialName("learner_reflection_text")
    val learnerReflectionText: String =
        LearnerArchetypeOption.MOISTURE_BLINDNESS.presetReflections[1].orEmpty(),
    @SerialName("latest_coaching_response")
    val latestCoachingResponse: CognitiveCoachingResponseDto? = null,
    @SerialName("attempt_history")
    val attemptHistory: Map<Int, CognitiveCoachingResponseDto> = emptyMap(),
    @SerialName("eval_suite_report")
    val evalSuiteReport: EvalSuiteReportDto? = null
)

/**
 * Atomic JSON disk persistence for [SessionSnapshot] across app process restarts.
 *
 * Persists user-configured LAN backend URL, Tapo camera configuration, longitudinal
 * attempt history (`Attempt 1 -> 2 -> 3`), and evaluation benchmark reports.
 */
@Singleton
class SessionPersistenceDataSource {

    private val storageFile: File
    private val json: Json
    private val lock = Any()

    @Inject
    constructor(
        @ApplicationContext context: Context,
        json: Json
    ) {
        this.storageFile = File(context.filesDir, DEFAULT_FILE_NAME)
        this.json = json
    }

    constructor(
        storageFile: File,
        json: Json
    ) {
        this.storageFile = storageFile
        this.json = json
    }

    fun loadSnapshot(): SessionSnapshot? = synchronized(lock) {
        if (!storageFile.exists()) return null
        return runCatching {
            val rawJson = storageFile.readText(Charsets.UTF_8)
            if (rawJson.isBlank()) return null
            val dto = json.decodeFromString(PersistedSessionSnapshotDto.serializer(), rawJson)
            dto.toDomain()
        }.getOrNull()
    }

    fun saveSnapshot(snapshot: SessionSnapshot) = synchronized(lock) {
        runCatching {
            storageFile.parentFile?.mkdirs()
            val tempFile = File(storageFile.parentFile, "${storageFile.name}.tmp")
            val encoded = json.encodeToString(
                PersistedSessionSnapshotDto.serializer(),
                snapshot.toPersistedDto()
            )
            tempFile.writeText(encoded, Charsets.UTF_8)
            if (!tempFile.renameTo(storageFile)) {
                storageFile.writeText(encoded, Charsets.UTF_8)
                tempFile.delete()
            }
        }
    }

    companion object {
        const val DEFAULT_FILE_NAME = "sizzlesense_session_snapshot.json"
    }
}

private fun SessionSnapshot.toPersistedDto(): PersistedSessionSnapshotDto = PersistedSessionSnapshotDto(
    backendBaseUrl = backendBaseUrl,
    tapoConfig = PersistedTapoCameraConfigDto(
        ipAddress = tapoConfig.ipAddress,
        username = tapoConfig.username,
        password = tapoConfig.password,
        useSubStream = tapoConfig.useSubStream,
        port = tapoConfig.port,
        isConnected = tapoConfig.isConnected
    ),
    selectedArchetype = selectedArchetype.name,
    currentAttemptNumber = currentAttemptNumber,
    learnerReflectionText = learnerReflectionText,
    latestCoachingResponse = latestCoachingResponse?.toDto(),
    attemptHistory = attemptHistory.mapValues { (_, response) -> response.toDto() },
    evalSuiteReport = evalSuiteReport?.toDto()
)

private fun PersistedSessionSnapshotDto.toDomain(): SessionSnapshot {
    val archetype = runCatching {
        LearnerArchetypeOption.valueOf(selectedArchetype)
    }.getOrDefault(LearnerArchetypeOption.MOISTURE_BLINDNESS)

    return SessionSnapshot(
        backendBaseUrl = backendBaseUrl,
        tapoConfig = TapoCameraConfig(
            ipAddress = tapoConfig.ipAddress,
            username = tapoConfig.username,
            password = tapoConfig.password,
            useSubStream = tapoConfig.useSubStream,
            port = tapoConfig.port,
            isConnected = tapoConfig.isConnected
        ),
        selectedArchetype = archetype,
        currentAttemptNumber = currentAttemptNumber.coerceIn(1, 3),
        learnerReflectionText = learnerReflectionText,
        latestCoachingResponse = latestCoachingResponse?.toDomain(),
        attemptHistory = attemptHistory.mapValues { (_, dto) -> dto.toDomain() },
        evalSuiteReport = evalSuiteReport?.toDomain()
    )
}
