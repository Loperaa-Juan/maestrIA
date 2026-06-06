package com.juanjoselopera.proy_prog_mobile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    // La UI solo ve materias no borradas. Flow para refrescar automáticamente.
    @Query("SELECT * FROM subjects WHERE deleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<SubjectEntity>>

    // Todas las filas, incluidas tombstones; lo usa el motor de sync.
    @Query("SELECT * FROM subjects")
    suspend fun getAll(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE pendingSync = 1")
    suspend fun getPending(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SubjectEntity)

    @Query("UPDATE subjects SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun hardDelete(id: String)
}
