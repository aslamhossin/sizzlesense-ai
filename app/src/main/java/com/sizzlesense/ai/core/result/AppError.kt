package com.sizzlesense.ai.core.result

/**
 * Structured domain error hierarchy mapped by [com.sizzlesense.ai.core.network.safeApiCall].
 */
sealed interface AppError {

    val readableMessage: String

    data class NetworkUnavailable(
        override val readableMessage: String = "Unable to reach the Python AI Backend on LAN."
    ) : AppError

    data class Timeout(
        override val readableMessage: String = "Request timed out while waiting for Gemini 3 reasoning."
    ) : AppError

    data class HttpError(
        val statusCode: Int,
        val responseBody: String,
        override val readableMessage: String = "Backend returned HTTP $statusCode: $responseBody"
    ) : AppError

    data class SerializationError(
        override val readableMessage: String = "Failed to parse backend JSON payload."
    ) : AppError

    data class ValidationError(
        override val readableMessage: String
    ) : AppError

    data class Unknown(
        override val readableMessage: String
    ) : AppError
}
