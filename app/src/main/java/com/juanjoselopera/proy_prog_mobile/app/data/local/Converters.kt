package com.juanjoselopera.proy_prog_mobile.app.data.local

import androidx.room.TypeConverter

// Convierte List<String> (tags) a/desde un String para almacenarlo en una columna.
// Se usa "\n" como separador; los tags no contienen saltos de línea.
class Converters {

    @TypeConverter
    fun fromTags(tags: List<String>): String = tags.joinToString("\n")

    @TypeConverter
    fun toTags(value: String): List<String> =
        // Se filtran las entradas vacías para que un tag en blanco no sea
        // indistinguible de una lista vacía en el round-trip.
        value.split("\n").filter { it.isNotEmpty() }
}
