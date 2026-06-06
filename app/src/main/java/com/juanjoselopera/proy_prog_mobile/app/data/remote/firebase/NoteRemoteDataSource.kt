package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NoteRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NoteSyncSource {

    private val notesCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("notes")
        }

    override suspend fun fetchAll(): List<NoteDto> {
        val collection = notesCollection ?: return emptyList()
        return collection.get().await().documents.mapNotNull { doc ->
            doc.toObject(NoteDto::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun upsert(dto: NoteDto) {
        val collection = notesCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(dto.id).set(dto).await()
    }

    override suspend fun delete(id: String) {
        val collection = notesCollection ?: return
        collection.document(id).delete().await()
    }
}
