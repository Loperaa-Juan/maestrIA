package com.juanjoselopera.proy_prog_mobile.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Representa una fila en la tabla "materias" de la base de datos local
@Entity(tableName = "materias")
data class MateriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    // Se guarda el índice del ícono (no el ID del recurso) porque los IDs
    // de recursos cambian entre compilaciones y romperían la referencia
    val iconIndex: Int,
    // Igual que iconIndex: el índice apunta a la lista colorOptions del fragment
    val colorIndex: Int
)
