package com.sizzlesense.ai.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.repository.SizzleCognitiveRepository
import com.sizzlesense.ai.domain.repository.CognitiveRepository
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
import com.sizzlesense.ai.domain.usecase.RunHumanLearningEvalUseCase
import com.sizzlesense.ai.presentation.mvi.SizzleViewModel

/**
 * DIP (Dependency Inversion Principle) Composition Root:
 * Wires Data implementations to Domain interfaces and Use Cases in one place,
 * keeping Presentation (`SizzleViewModel`) decoupled from Data layer classes.
 */
class SizzleAppContainer(
    val repository: CognitiveRepository = SizzleCognitiveRepository(
        remoteClient = FailoverJsonHttpClient(),
        localFallback = LocalFallbackCognitiveDataSource(),
    ),
) {
    val analyzeCookingSessionUseCase = AnalyzeCookingSessionUseCase(repository)
    val captureLiveTapoSessionUseCase = CaptureLiveTapoSessionUseCase(repository)
    val runHumanLearningEvalUseCase = RunHumanLearningEvalUseCase(repository)

    val viewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SizzleViewModel(
                analyzeCookingSession = analyzeCookingSessionUseCase,
                captureLiveTapoSession = captureLiveTapoSessionUseCase,
                runHumanLearningEval = runHumanLearningEvalUseCase,
            ) as T
        }
    }
}
