package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note

data class NoteDto(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = 0L
) {
    fun toNote() = Note(
        id = id,
        title = title,
        content = content,
        subjectId = subjectId,
        subjectName = subjectName,
        tags = tags,
        createdAt = createdAt
    )

    companion object {
        fun fromNote(note: Note) = NoteDto(
            id = note.id,
            title = note.title,
            content = note.content,
            subjectId = note.subjectId,
            subjectName = note.subjectName,
            tags = note.tags,
            createdAt = note.createdAt
        )
    }
}
