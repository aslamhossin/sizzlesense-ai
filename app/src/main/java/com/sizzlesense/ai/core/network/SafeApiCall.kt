package com.sizzlesense.ai.core.network

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppException
import com.sizzlesense.ai.core.result.AppResult
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

/**
 * Executes a suspending network operation on [dispatcher], cooperatively propagating
 * [CancellationException] and mapping I/O, timeout, HTTP, and serialization failures to [AppError].
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    block: suspend () -> T
): AppResult<T> = withContext(dispatcher) {
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: AppException) {
        AppResult.Failure(e.appError)
    } catch (e: SocketTimeoutException) {
        AppResult.Failure(
            AppError.Timeout(
                e.message ?: "Request timed out while waiting for Gemini 3 reasoning."
            )
        )
    } catch (e: SerializationException) {
        AppResult.Failure(
            AppError.SerializationError(
                e.message ?: "Failed to parse backend JSON payload."
            )
        )
    } catch (e: IllegalArgumentException) {
        AppResult.Failure(
            AppError.ValidationError(
                e.message ?: "Invalid request input."
            )
        )
    } catch (e: IOException) {
        AppResult.Failure(
            AppError.NetworkUnavailable(
                e.message ?: "Network connection unavailable."
            )
        )
    } catch (e: Exception) {
        AppResult.Failure(
            AppError.Unknown(
                e.message ?: "Unexpected error occurred."
            )
        )
    }
}
