package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

data class NoteDto(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val imageUri: String? = null,
    val updatedAt: Long = 0L
)
