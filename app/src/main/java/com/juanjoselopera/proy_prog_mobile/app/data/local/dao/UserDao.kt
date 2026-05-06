package com.juanjoselopera.proy_prog_mobile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.UserEntity

// DAO: operaciones sobre la tabla "users". Solo se espera un usuario a la vez
@Dao
interface UserDao {

    // LIMIT 1 garantiza que solo se retorna el usuario activo
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    // Se llama al cerrar sesión para limpiar los datos del usuario
    @Query("DELETE FROM users")
    suspend fun clearUser()
}
