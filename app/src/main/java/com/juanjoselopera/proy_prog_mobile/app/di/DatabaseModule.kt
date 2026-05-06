package com.juanjoselopera.proy_prog_mobile.app.di

import android.content.Context
import androidx.room.Room
import com.juanjoselopera.proy_prog_mobile.app.data.local.AppDatabase
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.MateriaDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Módulo Hilt que le indica cómo construir e inyectar las dependencias de Room
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Crea la base de datos una sola vez y la reutiliza en toda la app
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "maestria_db")
            .build()

    // Los DAOs se obtienen desde la misma instancia de la base de datos
    @Provides
    @Singleton
    fun provideMateriaDao(db: AppDatabase): MateriaDao = db.materiaDao()

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}
