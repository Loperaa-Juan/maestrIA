package com.juanjoselopera.proy_prog_mobile.app.di

import com.juanjoselopera.proy_prog_mobile.app.core.connectivity.ConnectivityObserver
import com.juanjoselopera.proy_prog_mobile.app.core.connectivity.NetworkConnectivityObserver
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteRemoteDataSource
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteSyncSource
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectRemoteDataSource
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectSyncSource
import com.juanjoselopera.proy_prog_mobile.app.data.sync.SyncManager
import com.juanjoselopera.proy_prog_mobile.app.data.sync.SyncTrigger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(impl: NetworkConnectivityObserver): ConnectivityObserver

    @Binds
    @Singleton
    abstract fun bindSubjectSyncSource(impl: SubjectRemoteDataSource): SubjectSyncSource

    @Binds
    @Singleton
    abstract fun bindNoteSyncSource(impl: NoteRemoteDataSource): NoteSyncSource

    @Binds
    @Singleton
    abstract fun bindSyncTrigger(impl: SyncManager): SyncTrigger
}
