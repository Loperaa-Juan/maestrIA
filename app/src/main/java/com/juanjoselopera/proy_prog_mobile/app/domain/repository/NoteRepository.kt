package com.juanjoselopera.proy_prog_mobile.app.domain.repository

import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(subjectId: String? = null): Flow<List<Note>>
    suspend fun addNote(note: Note): String
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(noteId: String)
}
