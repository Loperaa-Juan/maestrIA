package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

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
) {
    private val subjectsCollection
        get() = firestore.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("subjects")

    fun getSubjects(): Flow<List<SubjectDto>> = callbackFlow {
        val listener = subjectsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val subjects = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(SubjectDto::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(subjects)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addSubject(subject: Subject): String {
        val dto = SubjectDto.fromSubject(subject)
        val docRef = subjectsCollection.add(dto).await()
        return docRef.id
    }

    suspend fun updateSubject(subject: Subject) {
        subjectsCollection.document(subject.id).set(SubjectDto.fromSubject(subject)).await()
    }

    suspend fun deleteSubject(subjectId: String) {
        subjectsCollection.document(subjectId).delete().await()
    }
}
