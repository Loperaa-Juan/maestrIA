package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dataSource: NoteRemoteDataSource
) : NoteRepository {

    override fun getNotes(subjectId: String?): Flow<List<Note>> =
        dataSource.getNotes(subjectId).map { dtos -> dtos.map { it.toNote() } }

    override suspend fun addNote(note: Note): String =
        dataSource.addNote(note)

    override suspend fun updateNote(note: Note) =
        dataSource.updateNote(note)

    override suspend fun deleteNote(noteId: String) =
        dataSource.deleteNote(noteId)
}
