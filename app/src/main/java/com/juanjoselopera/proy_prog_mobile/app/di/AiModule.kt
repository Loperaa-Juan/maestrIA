package com.juanjoselopera.proy_prog_mobile.app.di

import com.juanjoselopera.proy_prog_mobile.BuildConfig
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.AiApiService
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.AiRepositoryImpl
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.AuthInterceptor
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AiRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    companion object {

        @Provides
        @Singleton
        fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                builder.addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }

            return builder.build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(client: OkHttpClient): Retrofit =
            Retrofit.Builder()
                .baseUrl(BuildConfig.AI_API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        @Provides
        @Singleton
        fun provideAiApiService(retrofit: Retrofit): AiApiService =
            retrofit.create(AiApiService::class.java)
    }
}
