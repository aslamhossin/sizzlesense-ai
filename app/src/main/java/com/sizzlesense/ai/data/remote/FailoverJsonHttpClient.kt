package com.sizzlesense.ai.data.remote

import com.sizzlesense.ai.core.network.safeApiCall
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppException
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.di.qualifier.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class FailoverJsonHttpClient @Inject constructor(
    val json: Json,
    private val httpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun resolveCandidateEndpoints(primaryBaseUrl: String): List<String> {
        return listOf(
            primaryBaseUrl.trim().trimEnd('/'),
            EMULATOR_HOST_URL,
            MAC_WORKSTATION_LAN_URL,
            LOCALHOST_URL
        ).filter { it.isNotBlank() }.distinct()
    }

    suspend fun <T> postJsonWithFailover(
        primaryBaseUrl: String,
        endpointPath: String,
        jsonBody: String,
        responseSerializer: KSerializer<T>
    ): AppResult<T> = safeApiCall(ioDispatcher) {
        val candidates = resolveCandidateEndpoints(primaryBaseUrl)
        val mediaType = JSON_MEDIA_TYPE.toMediaType()
        var lastError: AppError = AppError.NetworkUnavailable("No candidate endpoints configured")

        for (rootUrl in candidates) {
            try {
                val request = Request.Builder()
                    .url("$rootUrl$endpointPath")
                    .post(jsonBody.toRequestBody(mediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body.string()
                    if (response.isSuccessful && rawBody.isNotBlank()) {
                        return@safeApiCall json.decodeFromString(responseSerializer, rawBody)
                    } else {
                        lastError = AppError.HttpError(
                            statusCode = response.code,
                            responseBody = rawBody
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = AppError.NetworkUnavailable(
                    e.message ?: "Endpoint $rootUrl unreachable"
                )
            }
        }
        throw AppException(lastError)
    }

    companion object {
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
        const val EMULATOR_HOST_URL = "http://10.0.2.2:8000"
        const val MAC_WORKSTATION_LAN_URL = "http://192.168.11.12:8000"
        const val LOCALHOST_URL = "http://127.0.0.1:8000"
    }
}
