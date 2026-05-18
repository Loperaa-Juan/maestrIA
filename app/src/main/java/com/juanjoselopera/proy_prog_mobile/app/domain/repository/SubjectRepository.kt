package com.juanjoselopera.proy_prog_mobile.app.domain.repository

import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getSubjects(): Flow<List<Subject>>
    suspend fun addSubject(subject: Subject): String
    suspend fun updateSubject(subject: Subject)
    suspend fun deleteSubject(subjectId: String)
}
