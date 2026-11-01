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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base [ViewModel] implementing [UnidirectionalViewModel] with Kotlin Explicit Backing Fields
 * (`field = MutableStateFlow(...)`), buffered one-off [effects], and [AppResult] / [AppError] handling.
 */
abstract class BaseViewModel<STATE, INTENT, EFFECT>(
    initialState: STATE
) : ViewModel(), UnidirectionalViewModel<STATE, INTENT, EFFECT> {

    final override val uiState: StateFlow<STATE>
        field = MutableStateFlow(initialState)

    private val effectChannel = Channel<EFFECT>(Channel.BUFFERED)
    override val effects: Flow<EFFECT> = effectChannel.receiveAsFlow()

    protected val currentState: STATE
        get() = uiState.value

    protected fun updateState(reducer: (STATE) -> STATE) {
        uiState.update(reducer)
    }

    protected fun emitEffect(effect: EFFECT) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    protected fun <T> Flow<T>.collectWhileSubscribed(
        stopTimeoutMillis: Long = 5_000L,
        action: suspend (T) -> Unit
    ) {
        this.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            replay = 1
        )
            .onEach(action)
            .launchIn(viewModelScope)
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
}
