package com.juanjoselopera.proy_prog_mobile.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fila local de un apunte. content guarda el markdown del apunte.
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val subjectId: String,
    // Denormalizado a propósito (evita joins en las listas offline). No hay
    // @ForeignKey hacia subjects porque los borrados son lógicos y los sincroniza
    // el motor de sync; el repositorio debe refrescar este nombre al renombrar la materia.
    val subjectName: String,
    val tags: List<String>,
    val createdAt: Long,
    val imageUri: String?,
    // Metadatos de sincronización
    val updatedAt: Long,
    val pendingSync: Boolean,
    val deleted: Boolean
)
