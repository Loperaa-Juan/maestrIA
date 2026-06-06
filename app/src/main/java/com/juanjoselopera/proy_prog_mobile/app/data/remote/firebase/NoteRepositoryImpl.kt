package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.NoteDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.NoteEntity
import com.juanjoselopera.proy_prog_mobile.app.data.local.mapper.toDomain
import com.juanjoselopera.proy_prog_mobile.app.data.sync.SyncTrigger
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

// Repositorio offline-first respaldado por Room (pese al paquete .firebase). Firestore solo interviene vía el motor de sync.
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val syncTrigger: SyncTrigger
) : NoteRepository {

    override fun getNotes(subjectId: String?): Flow<List<Note>> {
        val source = if (subjectId != null) noteDao.observeBySubject(subjectId) else noteDao.observeAll()
        return source.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addNote(note: Note): String {
        // Si el id viene vacío se genera uno nuevo; si el llamador ya trae id, el upsert es intencional.
        val id = note.id.ifEmpty { UUID.randomUUID().toString() }
        noteDao.upsert(note.toPendingEntity(id))
        syncTrigger.requestSync()
        return id
    }

    override suspend fun updateNote(note: Note) {
        val existing = noteDao.getById(note.id) ?: return
        noteDao.upsert(
            existing.copy(
                title = note.title,
                content = note.content,
                subjectId = note.subjectId,
                subjectName = note.subjectName,
                tags = note.tags,
                imageUri = note.imageUri,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
        )
        syncTrigger.requestSync()
    }

    override suspend fun deleteNote(noteId: String) {
        val existing = noteDao.getById(noteId) ?: return
        noteDao.upsert(
            existing.copy(
                updatedAt = System.currentTimeMillis(),
                pendingSync = true,
                deleted = true
            )
        )
        syncTrigger.requestSync()
    }

    // Construye una entidad marcada como pendiente a partir del modelo de dominio.
    private fun Note.toPendingEntity(id: String) = NoteEntity(
        id = id,
        title = title,
        content = content,
        subjectId = subjectId,
        subjectName = subjectName,
        tags = tags,
        createdAt = createdAt,
        imageUri = imageUri,
        updatedAt = System.currentTimeMillis(),
        pendingSync = true,
        deleted = false
    )
}
