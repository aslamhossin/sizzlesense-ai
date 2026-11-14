package com.sizzlesense.ai.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppException
import com.sizzlesense.ai.core.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Ephemeral per-screen async execution status combined with [SessionStore] state.
 */
data class TaskStatus(
    val isRunning: Boolean = false,
    val label: String = "Ready",
    val errorMessage: String? = null
)

/**
 * Base [ViewModel] implementing [UnidirectionalViewModel] with lifecycle-aware derived state,
 * buffered one-off [effects], and guarded [AppResult] / [AppError] task execution.
 */
abstract class BaseViewModel<STATE, INTENT, EFFECT> :
    ViewModel(),
    UnidirectionalViewModel<STATE, INTENT, EFFECT> {

    private val effectChannel = Channel<EFFECT>(Channel.BUFFERED)
    override val effects: Flow<EFFECT> = effectChannel.receiveAsFlow()

    protected fun emitEffect(effect: EFFECT) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    protected fun <T> Flow<T>.stateWhileSubscribed(
        initialValue: T,
        stopTimeoutMillis: Long = DEFAULT_STOP_TIMEOUT_MS
    ): StateFlow<T> = stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = initialValue
    )

    /**
     * Runs one [AppResult] task at a time against this [TaskStatus] flow, committing domain
     * changes via [onSuccess] before clearing the running flag and emitting [effectOnSuccess].
     */
    protected fun <T> MutableStateFlow<TaskStatus>.launchGuardedTask(
        runningLabel: String = "Working...",
        completedLabel: String = "Done",
        failedLabel: String = "Unable to complete",
        effectOnSuccess: EFFECT? = null,
        onSuccess: suspend (T) -> Unit,
        block: suspend () -> AppResult<T>
    ) {
        // Atomic check-and-set stops duplicate parallel inference/benchmark calls on rapid taps.
        var acquired = false
        update { current ->
            if (current.isRunning) {
                acquired = false
                current
            } else {
                acquired = true
                TaskStatus(isRunning = true, label = runningLabel, errorMessage = null)
            }
        }
        if (!acquired) return

        launchResultTask(
            onSuccess = { data ->
                onSuccess(data)
                value = TaskStatus(
                    isRunning = false,
                    label = completedLabel,
                    errorMessage = null
                )
                if (effectOnSuccess != null) {
                    emitEffect(effectOnSuccess)
                }
            },
            onError = { appError ->
                value = TaskStatus(
                    isRunning = false,
                    label = failedLabel,
                    errorMessage = appError.readableMessage
                )
            },
            block = block
        )
    }

    protected fun <T> launchResultTask(
        onSuccess: suspend (T) -> Unit,
        onError: (AppError) -> Unit = {},
        block: suspend () -> AppResult<T>
    ) {
        viewModelScope.launch {
            try {
                when (val result = block()) {
                    is AppResult.Success -> onSuccess(result.data)
                    is AppResult.Failure -> onError(result.error)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppException) {
                onError(e.appError)
            } catch (e: IllegalArgumentException) {
                onError(AppError.ValidationError(e.message ?: "Invalid input"))
            } catch (e: Exception) {
                onError(AppError.Unknown(e.message ?: "Unexpected error occurred"))
            }
        }
    }

    private companion object {
        const val DEFAULT_STOP_TIMEOUT_MS = 5_000L
    }
}
