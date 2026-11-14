package com.sizzlesense.ai.core.result

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
        override val readableMessage: String = "Backend returned HTTP $statusCode: $responseBody"
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
}
