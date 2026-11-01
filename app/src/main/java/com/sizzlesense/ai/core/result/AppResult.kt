package com.sizzlesense.ai.core.result

/**
 * Type-safe result wrapper for network and repository operations.
 */
sealed interface AppResult<out T> {

    data class Success<T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.getOrElse(onFailure: (AppError) -> T): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Failure -> onFailure(error)
}

fun <T> AppResult<T>.getOrThrow(): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Failure -> throw AppException(error)
}
