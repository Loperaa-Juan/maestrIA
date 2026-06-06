package com.juanjoselopera.proy_prog_mobile.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.NoteDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.SubjectDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.NoteEntity
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.SubjectEntity

@Database(
    entities = [SubjectEntity::class, NoteEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun noteDao(): NoteDao
}
