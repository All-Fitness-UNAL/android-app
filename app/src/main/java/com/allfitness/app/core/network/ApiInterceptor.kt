package com.allfitness.app.core.network

import com.allfitness.app.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Marca los endpoints de IA, que pueden tardar hasta 60 s (research R1). */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LongRunning

class ApiInterceptor @Inject constructor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            sessionManager.token?.let { header("Authorization", "Bearer $it") }
        }.build()
        val longRunning = request.tag(Invocation::class.java)?.method()
            ?.isAnnotationPresent(LongRunning::class.java) == true
        val call = if (longRunning) chain.withReadTimeout(AI_TIMEOUT_SECONDS, TimeUnit.SECONDS) else chain
        return call.proceed(request)
    }

    private companion object {
        const val AI_TIMEOUT_SECONDS = 90
    }
}
