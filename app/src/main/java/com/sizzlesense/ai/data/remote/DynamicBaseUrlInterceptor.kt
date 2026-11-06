package com.sizzlesense.ai.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp [Interceptor] that rewrites the request's scheme, host, and port when
 * [SizzleSenseApiService.HEADER_TARGET_BASE_URL] is supplied on a Retrofit call.
 */
class DynamicBaseUrlInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val targetBaseUrlHeader = originalRequest.header(SizzleSenseApiService.HEADER_TARGET_BASE_URL)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return chain.proceed(originalRequest)

        val targetHttpUrl = targetBaseUrlHeader.toHttpUrlOrNull()
            ?: return chain.proceed(
                originalRequest.newBuilder()
                    .removeHeader(SizzleSenseApiService.HEADER_TARGET_BASE_URL)
                    .build()
            )

        val rewrittenUrl = originalRequest.url.newBuilder()
            .scheme(targetHttpUrl.scheme)
            .host(targetHttpUrl.host)
            .port(targetHttpUrl.port)
            .build()

        val rewrittenRequest = originalRequest.newBuilder()
            .removeHeader(SizzleSenseApiService.HEADER_TARGET_BASE_URL)
            .url(rewrittenUrl)
            .build()

        return chain.proceed(rewrittenRequest)
    }
}
