package com.sizzlesense.ai.presentation.mvi

sealed interface SizzleEffect {
    data class ShowToast(val message: String) : SizzleEffect
}
