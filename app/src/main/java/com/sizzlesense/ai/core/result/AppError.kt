package com.sizzlesense.ai.core.result

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Structured domain error hierarchy mapped by [com.sizzlesense.ai.core.network.safeApiCall].
 */
sealed interface AppError {

    val readableMessage: String

    data class NetworkUnavailable(
        override val readableMessage: String = "Couldn't reach the coaching server."
    ) : AppError

    data class Timeout(
        override val readableMessage: String = "Analysis timed out. Please try again."
    ) : AppError

    data class HttpError(
        val statusCode: Int,
        val responseBody: String,
        override val readableMessage: String = formatHttpErrorMessage(statusCode, responseBody)
    ) : AppError

    data class SerializationError(
        override val readableMessage: String = "Received an unexpected response from the server."
    ) : AppError

    data class ValidationError(
        override val readableMessage: String
    ) : AppError

    data class Unknown(
        override val readableMessage: String
    ) : AppError

    private companion object {
        fun formatHttpErrorMessage(statusCode: Int, responseBody: String): String {
            val trimmed = responseBody.trim()
            if (trimmed.isEmpty()) {
                return "Backend returned HTTP $statusCode"
            }
            val detail = extractFastApiDetail(trimmed)
            if (!detail.isNullOrBlank()) {
                return detail
            }
            return "Backend returned HTTP $statusCode: $trimmed"
        }

        private fun extractFastApiDetail(rawBody: String): String? = runCatching {
            val root = Json.parseToJsonElement(rawBody) as? JsonObject ?: return null
            when (val detailElement = root["detail"]) {
                is JsonPrimitive -> detailElement.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
                is JsonArray -> detailElement.mapNotNull { item ->
                    when (item) {
                        is JsonObject -> (item["msg"] as? JsonPrimitive)?.contentOrNull?.trim()
                        is JsonPrimitive -> item.contentOrNull?.trim()
                        else -> null
                    }
                }.filter { it.isNotEmpty() }.joinToString("; ").takeIf { it.isNotEmpty() }
                else -> null
            }
        }.getOrNull()
    }
}
