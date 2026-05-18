package com.juanjoselopera.proy_prog_mobile.app.domain.model

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
