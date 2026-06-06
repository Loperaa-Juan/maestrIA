package com.juanjoselopera.proy_prog_mobile.app.di

import android.content.Context
import androidx.room.Room
import com.juanjoselopera.proy_prog_mobile.app.data.local.AppDatabase
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.NoteDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.SubjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "maestria_db")
            // Esquema cambió por completo respecto a v1 y no hay datos en producción.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()

    @Provides
    @Singleton
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
}
