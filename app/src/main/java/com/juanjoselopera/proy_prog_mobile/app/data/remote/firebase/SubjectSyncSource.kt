package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

// Superficie mínima que el motor de sync usa contra Firestore para materias.
interface SubjectSyncSource {
    suspend fun fetchAll(): List<SubjectDto>
    suspend fun upsert(dto: SubjectDto)
    suspend fun delete(id: String)
}
