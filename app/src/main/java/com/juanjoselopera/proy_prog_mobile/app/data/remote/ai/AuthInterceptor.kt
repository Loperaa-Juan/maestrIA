package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("User not authenticated")

        val token = runBlocking { user.getIdToken(false).await() }.token
            ?: throw IllegalStateException("Could not retrieve Firebase ID token")

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}
