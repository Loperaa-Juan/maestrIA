package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

interface NoteSyncSource {
    suspend fun fetchAll(): List<NoteDto>
    suspend fun upsert(dto: NoteDto)
    suspend fun delete(id: String)
}
