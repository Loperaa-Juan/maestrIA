package com.juanjoselopera.proy_prog_mobile.app.di

import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteRepositoryImpl
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectRepositoryImpl
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SubjectNoteModule {

    @Binds
    @Singleton
    abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}
