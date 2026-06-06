package com.juanjoselopera.proy_prog_mobile.app.data.sync

import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.NoteDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.SubjectDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.mapper.toDto
import com.juanjoselopera.proy_prog_mobile.app.data.local.mapper.toEntity
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteSyncSource
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectSyncSource
import javax.inject.Inject

// Reconciliación pura Room <-> Firestore por last-write-wins. Sin dependencias de
// Android ni Firebase: totalmente testeable con fakes.
class SyncEngine @Inject constructor(
    private val subjectDao: SubjectDao,
    private val noteDao: NoteDao,
    private val subjectRemote: SubjectSyncSource,
    private val noteRemote: NoteSyncSource,
) {

    suspend fun sync() {
        // Se sincronizan en secuencia: si falla syncSubjects(), syncNotes() no corre en este ciclo (el llamador envuelve sync() en runCatching).
        syncSubjects()
        syncNotes()
    }

    private suspend fun syncSubjects() {
        // 1) Push de cambios locales pendientes.
        val justPushed = mutableSetOf<String>()
        subjectDao.getPending().forEach { local ->
            if (local.deleted) {
                subjectRemote.delete(local.id)
                subjectDao.hardDelete(local.id)
            } else {
                subjectRemote.upsert(local.toDto())
                subjectDao.markSynced(local.id)
                justPushed.add(local.id)
            }
        }
        // 2) Pull: traer remoto y aplicar last-write-wins.
        val remote = subjectRemote.fetchAll()
        val remoteIds = remote.map { it.id }.toSet()
        remote.forEach { dto ->
            val local = subjectDao.getById(dto.id)
            if (local == null || (local.id !in justPushed && !local.pendingSync && dto.updatedAt > local.updatedAt)) { // Empate de timestamps: gana lo local (comparación estricta >).
                subjectDao.upsert(dto.toEntity(pendingSync = false))
            }
        }
        // 3) Borrados remotos: filas locales ya sincronizadas que ya no están en remoto.
        subjectDao.getAll().forEach { local ->
            if (!local.pendingSync && !local.deleted &&
                local.id !in remoteIds && local.id !in justPushed
            ) {
                subjectDao.hardDelete(local.id)
            }
        }
    }

    private suspend fun syncNotes() {
        val justPushed = mutableSetOf<String>()
        noteDao.getPending().forEach { local ->
            if (local.deleted) {
                noteRemote.delete(local.id)
                noteDao.hardDelete(local.id)
            } else {
                noteRemote.upsert(local.toDto())
                noteDao.markSynced(local.id)
                justPushed.add(local.id)
            }
        }
        val remote = noteRemote.fetchAll()
        val remoteIds = remote.map { it.id }.toSet()
        remote.forEach { dto ->
            val local = noteDao.getById(dto.id)
            if (local == null || (local.id !in justPushed && !local.pendingSync && dto.updatedAt > local.updatedAt)) {
                noteDao.upsert(dto.toEntity(pendingSync = false))
            }
        }
        noteDao.getAll().forEach { local ->
            if (!local.pendingSync && !local.deleted &&
                local.id !in remoteIds && local.id !in justPushed
            ) {
                noteDao.hardDelete(local.id)
            }
        }
    }
}
