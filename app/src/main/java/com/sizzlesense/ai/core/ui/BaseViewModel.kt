package com.sizzlesense.ai.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base [ViewModel] implementing [UnidirectionalViewModel] with buffered one-off [effects]
 * and cooperative [CancellationException]-safe coroutine execution mapped to [AppError].
 */
abstract class BaseViewModel<STATE, INTENT, EFFECT>(
    initialState: STATE
) : ViewModel(), UnidirectionalViewModel<STATE, INTENT, EFFECT> {

    private val _uiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<STATE> = _uiState.asStateFlow()

    private val effectChannel = Channel<EFFECT>(Channel.BUFFERED)
    override val effects: Flow<EFFECT> = effectChannel.receiveAsFlow()

    protected val currentState: STATE
        get() = _uiState.value

    protected fun updateState(reducer: (STATE) -> STATE) {
        _uiState.update(reducer)
    }

    protected fun emitEffect(effect: EFFECT) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    protected fun launchTask(
        onError: (AppError) -> Unit = {},
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
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
