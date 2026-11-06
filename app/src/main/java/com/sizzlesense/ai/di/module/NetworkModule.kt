package com.sizzlesense.ai.di.module

import com.sizzlesense.ai.data.remote.DynamicBaseUrlInterceptor
import com.sizzlesense.ai.data.remote.SizzleSenseApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Provides shared networking singletons (`Json`, `OkHttpClient`, and Retrofit `SizzleSenseApiService`).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 2L
    private const val READ_TIMEOUT_SECONDS = 28L
    private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(DynamicBaseUrlInterceptor())
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideSizzleSenseApiService(
        json: Json,
        okHttpClient: OkHttpClient
    ): SizzleSenseApiService = Retrofit.Builder()
        .baseUrl(SizzleSenseApiService.DEFAULT_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
        .build()
        .create(SizzleSenseApiService::class.java)
}
