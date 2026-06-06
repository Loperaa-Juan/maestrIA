package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SubjectRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SubjectSyncSource {

    private val subjectsCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("subjects")
        }

    override suspend fun fetchAll(): List<SubjectDto> {
        val collection = subjectsCollection ?: return emptyList()
        return collection.get().await().documents.mapNotNull { doc ->
            doc.toObject(SubjectDto::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun upsert(dto: SubjectDto) {
        val collection = subjectsCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(dto.id).set(dto).await()
    }

    override suspend fun delete(id: String) {
        val collection = subjectsCollection ?: return
        collection.document(id).delete().await()
    }
}
