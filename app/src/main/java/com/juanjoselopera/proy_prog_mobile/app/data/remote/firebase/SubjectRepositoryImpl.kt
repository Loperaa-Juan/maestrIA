package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.SubjectDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.SubjectEntity
import com.juanjoselopera.proy_prog_mobile.app.data.local.mapper.toDomain
import com.juanjoselopera.proy_prog_mobile.app.data.sync.SyncTrigger
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

// Repositorio offline-first respaldado por Room (pese al paquete .firebase). Firestore solo interviene vía el motor de sync.
class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
    private val syncTrigger: SyncTrigger
) : SubjectRepository {

    override fun getSubjects(): Flow<List<Subject>> =
        subjectDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addSubject(subject: Subject): String {
        // Si el id viene vacío se genera uno nuevo; si el llamador ya trae id, el upsert es intencional.
        val id = subject.id.ifEmpty { UUID.randomUUID().toString() }
        subjectDao.upsert(
            SubjectEntity(
                id = id,
                name = subject.name,
                iconIndex = subject.iconIndex,
                colorIndex = subject.colorIndex,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true,
                deleted = false
            )
        )
        syncTrigger.requestSync()
        return id
    }

    override suspend fun updateSubject(subject: Subject) {
        val existing = subjectDao.getById(subject.id) ?: return
        subjectDao.upsert(
            existing.copy(
                name = subject.name,
                iconIndex = subject.iconIndex,
                colorIndex = subject.colorIndex,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
        )
        syncTrigger.requestSync()
    }

    override suspend fun deleteSubject(subjectId: String) {
        val existing = subjectDao.getById(subjectId) ?: return
        subjectDao.upsert(
            existing.copy(
                updatedAt = System.currentTimeMillis(),
                pendingSync = true,
                deleted = true
            )
        )
        syncTrigger.requestSync()
    }
}
