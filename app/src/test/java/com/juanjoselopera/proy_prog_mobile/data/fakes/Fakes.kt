package com.juanjoselopera.proy_prog_mobile.data.fakes

import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.NoteDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.SubjectDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.NoteEntity
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.SubjectEntity
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteSyncSource
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectSyncSource
import com.juanjoselopera.proy_prog_mobile.app.data.sync.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSubjectDao : SubjectDao {
    val rows = MutableStateFlow<List<SubjectEntity>>(emptyList())
    override fun observeAll(): Flow<List<SubjectEntity>> =
        rows.map { list -> list.filter { !it.deleted }.sortedBy { it.name } }
    override suspend fun getAll(): List<SubjectEntity> = rows.value
    override suspend fun getPending(): List<SubjectEntity> = rows.value.filter { it.pendingSync }
    override suspend fun getById(id: String): SubjectEntity? = rows.value.find { it.id == id }
    override suspend fun upsert(entity: SubjectEntity) {
        rows.value = rows.value.filter { it.id != entity.id } + entity
    }
    override suspend fun markSynced(id: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(pendingSync = false) else it }
    }
    override suspend fun hardDelete(id: String) {
        rows.value = rows.value.filter { it.id != id }
    }
}

class FakeNoteDao : NoteDao {
    val rows = MutableStateFlow<List<NoteEntity>>(emptyList())
    override fun observeAll(): Flow<List<NoteEntity>> =
        rows.map { list -> list.filter { !it.deleted }.sortedByDescending { it.createdAt } }
    override fun observeBySubject(subjectId: String): Flow<List<NoteEntity>> =
        rows.map { list -> list.filter { !it.deleted && it.subjectId == subjectId }.sortedByDescending { it.createdAt } }
    override suspend fun getAll(): List<NoteEntity> = rows.value
    override suspend fun getPending(): List<NoteEntity> = rows.value.filter { it.pendingSync }
    override suspend fun getById(id: String): NoteEntity? = rows.value.find { it.id == id }
    override suspend fun upsert(entity: NoteEntity) {
        rows.value = rows.value.filter { it.id != entity.id } + entity
    }
    override suspend fun markSynced(id: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(pendingSync = false) else it }
    }
    override suspend fun hardDelete(id: String) {
        rows.value = rows.value.filter { it.id != id }
    }
}

class FakeSubjectSyncSource : SubjectSyncSource {
    val remote = mutableMapOf<String, SubjectDto>()
    val deleted = mutableListOf<String>()
    override suspend fun fetchAll(): List<SubjectDto> = remote.values.toList()
    override suspend fun upsert(dto: SubjectDto) { remote[dto.id] = dto }
    override suspend fun delete(id: String) { remote.remove(id); deleted.add(id) }
}

class FakeNoteSyncSource : NoteSyncSource {
    val remote = mutableMapOf<String, NoteDto>()
    val deleted = mutableListOf<String>()
    override suspend fun fetchAll(): List<NoteDto> = remote.values.toList()
    override suspend fun upsert(dto: NoteDto) { remote[dto.id] = dto }
    override suspend fun delete(id: String) { remote.remove(id); deleted.add(id) }
}

class FakeSyncTrigger : SyncTrigger {
    var count = 0
    override fun requestSync() { count++ }
}
