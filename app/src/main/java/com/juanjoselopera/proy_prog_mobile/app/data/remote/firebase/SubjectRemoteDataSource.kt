package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SubjectRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SubjectSyncSource {
    private val subjectsCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("subjects")
        }

    fun getSubjects(): Flow<List<SubjectDto>> = callbackFlow {
        val collection = subjectsCollection
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // No propagamos la excepción: tumbaría la app desde un callback en el hilo Main.
                // Degradamos a lista vacía y dejamos rastro para diagnosticar (p. ej. PERMISSION_DENIED).
                Log.w("SubjectRemoteDataSource", "Firestore listen failed", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val subjects = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(SubjectDto::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(subjects)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addSubject(subject: Subject): String {
        val dto = SubjectDto.fromSubject(subject)
        val collection = subjectsCollection ?: throw IllegalStateException("User not authenticated")
        val docRef = collection.add(dto).await()
        return docRef.id
    }

    suspend fun updateSubject(subject: Subject) {
        val collection = subjectsCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(subject.id).set(SubjectDto.fromSubject(subject)).await()
    }

    suspend fun deleteSubject(subjectId: String) {
        val collection = subjectsCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(subjectId).delete().await()
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
        val collection = subjectsCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(id).delete().await()
    }
}
