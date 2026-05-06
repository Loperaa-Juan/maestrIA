package com.juanjoselopera.proy_prog_mobile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.MateriaEntity
import kotlinx.coroutines.flow.Flow

// DAO: define las operaciones SQL que se pueden hacer sobre la tabla "materias"
@Dao
interface MateriaDao {

    // Flow hace que la UI se actualice automáticamente cada vez que cambian los datos
    @Query("SELECT * FROM materias ORDER BY id ASC")
    fun getAll(): Flow<List<MateriaEntity>>

    // Se usa para saber si la BD está vacía y sembrar datos iniciales
    @Query("SELECT COUNT(*) FROM materias")
    suspend fun count(): Int

    // REPLACE: si ya existe una materia con el mismo ID, la sobreescribe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(materia: MateriaEntity)

    @Delete
    suspend fun delete(materia: MateriaEntity)

    @Query("DELETE FROM materias")
    suspend fun clearAll()
}
