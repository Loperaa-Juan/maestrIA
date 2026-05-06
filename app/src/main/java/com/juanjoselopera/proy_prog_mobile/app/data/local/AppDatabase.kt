package com.juanjoselopera.proy_prog_mobile.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.MateriaDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.UserDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.MateriaEntity
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.UserEntity

// Punto de entrada a la base de datos Room. exportSchema=false evita generar
// archivos de esquema innecesarios durante el desarrollo
@Database(
    entities = [MateriaEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materiaDao(): MateriaDao
    abstract fun userDao(): UserDao
}
