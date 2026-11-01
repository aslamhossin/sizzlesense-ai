package com.sizzlesense.ai.di.module

import com.sizzlesense.ai.data.repository.SizzleCognitiveRepository
import com.sizzlesense.ai.data.session.InMemorySessionStore
import com.sizzlesense.ai.domain.repository.EvaluationBenchmarkGateway
import com.sizzlesense.ai.domain.repository.LiveTapoCameraGateway
import com.sizzlesense.ai.domain.repository.SessionAnalysisGateway
import com.sizzlesense.ai.domain.repository.SessionStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds Domain session store and segregated gateway interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

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

    @Binds
    @Singleton
    abstract fun bindSessionStore(
        impl: InMemorySessionStore
    ): SessionStore
}
