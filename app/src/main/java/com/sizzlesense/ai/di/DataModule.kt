package com.sizzlesense.ai.di

import com.sizzlesense.ai.data.repository.SizzleCognitiveRepository
import com.sizzlesense.ai.domain.repository.CognitiveRepository
import com.sizzlesense.ai.domain.repository.EvaluationBenchmarkGateway
import com.sizzlesense.ai.domain.repository.LiveTapoCameraGateway
import com.sizzlesense.ai.domain.repository.SessionAnalysisGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds Domain repository and segregated gateway interfaces to [SizzleCognitiveRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindCognitiveRepository(
        impl: SizzleCognitiveRepository
    ): CognitiveRepository

    @Binds
    @Singleton
    abstract fun bindSessionAnalysisGateway(
        impl: SizzleCognitiveRepository
    ): SessionAnalysisGateway

    @Binds
    @Singleton
    abstract fun bindLiveTapoCameraGateway(
        impl: SizzleCognitiveRepository
    ): LiveTapoCameraGateway

    @Binds
    @Singleton
    abstract fun bindEvaluationBenchmarkGateway(
        impl: SizzleCognitiveRepository
    ): EvaluationBenchmarkGateway
}
