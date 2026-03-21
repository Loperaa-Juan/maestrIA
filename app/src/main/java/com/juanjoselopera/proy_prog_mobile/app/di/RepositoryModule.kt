package com.juanjoselopera.proy_prog_mobile.app.di

import com.juanjoselopera.proy_prog_mobile.app.data.remote.FirebaseAuthRepositoryImpl
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: FirebaseAuthRepositoryImpl
    ): AuthRepository

}