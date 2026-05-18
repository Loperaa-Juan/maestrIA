package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NoteRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val notesCollection
        get() = firestore.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("notes")

    fun getNotes(subjectId: String? = null): Flow<List<NoteDto>> = callbackFlow {
        val query = if (subjectId != null) {
            notesCollection.whereEqualTo("subjectId", subjectId)
        } else {
            notesCollection
        }
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val notes = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(NoteDto::class.java)?.copy(id = doc.id)
            }?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(notes)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addNote(note: Note): String {
        val dto = NoteDto.fromNote(note)
        val docRef = notesCollection.add(dto).await()
        return docRef.id
    }

    suspend fun updateNote(note: Note) {
        notesCollection.document(note.id).set(NoteDto.fromNote(note)).await()
    }

    suspend fun deleteNote(noteId: String) {
        notesCollection.document(noteId).delete().await()
    }
}
