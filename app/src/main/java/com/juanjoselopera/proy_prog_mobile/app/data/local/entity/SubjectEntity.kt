package com.juanjoselopera.proy_prog_mobile.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fila local de una materia. El id es un UUID generado en cliente que también
// se usa como id de documento en Firestore, para tener identidad estable offline.
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconIndex: Int,
    val colorIndex: Int,
    // Metadatos de sincronización
    val updatedAt: Long,      // última modificación local (para last-write-wins)
    val pendingSync: Boolean, // hay cambios locales sin enviar a Firestore
    val deleted: Boolean      // tombstone: borrado lógico hasta confirmarlo en remoto
)
