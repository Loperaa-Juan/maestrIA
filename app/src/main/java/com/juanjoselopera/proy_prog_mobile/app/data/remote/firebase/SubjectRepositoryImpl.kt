package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val dataSource: SubjectRemoteDataSource
) : SubjectRepository {

    override fun getSubjects(): Flow<List<Subject>> =
        dataSource.getSubjects().map { dtos -> dtos.map { it.toSubject() } }

    override suspend fun addSubject(subject: Subject): String =
        dataSource.addSubject(subject)

    override suspend fun updateSubject(subject: Subject) =
        dataSource.updateSubject(subject)

    override suspend fun deleteSubject(subjectId: String) =
        dataSource.deleteSubject(subjectId)
}
