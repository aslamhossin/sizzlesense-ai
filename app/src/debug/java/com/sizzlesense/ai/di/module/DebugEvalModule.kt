package com.sizzlesense.ai.di.module

import androidx.compose.runtime.Composable
import com.sizzlesense.ai.di.qualifier.DeveloperEvalsEnabled
import com.sizzlesense.ai.presentation.evalbench.EvalBenchRoute
import com.sizzlesense.ai.presentation.navigation.EvalBenchEntry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DebugEvalModule {

    @Provides
    @Singleton
    @DeveloperEvalsEnabled
    fun provideDeveloperEvalsEnabled(): Boolean = true

    @Provides
    @Singleton
    fun provideEvalBenchEntry(): EvalBenchEntry = object : EvalBenchEntry {
        @Composable
        override fun Content() {
            EvalBenchRoute()
        }
    }
}
