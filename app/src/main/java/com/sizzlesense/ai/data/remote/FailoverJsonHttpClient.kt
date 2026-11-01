package com.sizzlesense.ai.data.remote

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * DRY + SRP:
 * Single responsibility HTTP JSON client that automatically fails over across:
 *   1. User-configured base URL
 *   2. Android Emulator loopback (`http://10.0.2.2:8000`)
 *   3. Local Mac LAN workstation (`http://192.168.11.12:8000`)
 *   4. Localhost (`http://127.0.0.1:8000`)
 *
 * Eliminates repeating candidate URL resolution and OkHttp execution logic across endpoints.
 */
class FailoverJsonHttpClient(
    val json: Json = defaultJson(),
    private val httpClient: OkHttpClient = defaultOkHttpClient(),
) {

    fun resolveCandidateEndpoints(primaryBaseUrl: String): List<String> =
        listOf(
            primaryBaseUrl.trim().trimEnd('/'),
            EMULATOR_HOST_URL,
            MAC_WORKSTATION_LAN_URL,
            LOCALHOST_URL,
        ).filter { it.isNotBlank() }.distinct()

    suspend fun <T> postJsonWithFailover(
        primaryBaseUrl: String,
        endpointPath: String,
        jsonBody: String,
        responseSerializer: KSerializer<T>,
    ): Result<T> = withContext(Dispatchers.IO) {
        val candidates = resolveCandidateEndpoints(primaryBaseUrl)
        val mediaType = JSON_MEDIA_TYPE.toMediaType()
        var lastError: Throwable = IllegalStateException("No candidate endpoints configured")

        for (rootUrl in candidates) {
            try {
                val request = Request.Builder()
                    .url("$rootUrl$endpointPath")
                    .post(jsonBody.toRequestBody(mediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body.string()
                    if (response.isSuccessful && rawBody.isNotBlank()) {
                        val parsed = json.decodeFromString(responseSerializer, rawBody)
                        return@withContext Result.success(parsed)
                    } else {
                        lastError = IllegalStateException("HTTP ${response.code}: $rawBody")
                    }
                }
            } catch (e: Exception) {
                lastError = e
            }
        }
        Result.failure(lastError)
    }

    companion object {
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
        const val EMULATOR_HOST_URL = "http://10.0.2.2:8000"
        const val MAC_WORKSTATION_LAN_URL = "http://192.168.11.12:8000"
        const val LOCALHOST_URL = "http://127.0.0.1:8000"

        fun defaultJson(): Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun defaultOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(28, TimeUnit.SECONDS)
            .build()
    }
}
