package com.juanjoselopera.proy_prog_mobile.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Representa el usuario autenticado guardado localmente para consultas offline
@Entity(tableName = "users")
data class UserEntity(
    // El UID de Firebase se usa como clave primaria para evitar duplicados
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String? = null
)
