package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import android.util.Log
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
) : NoteSyncSource {
    private val notesCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("notes")
        }

    fun getNotes(subjectId: String? = null): Flow<List<NoteDto>> = callbackFlow {
        val collection = notesCollection
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val query = if (subjectId != null) {
            collection.whereEqualTo("subjectId", subjectId)
        } else {
            collection
        }
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // No propagamos la excepción: tumbaría la app desde un callback en el hilo Main.
                // Degradamos a lista vacía y dejamos rastro para diagnosticar (p. ej. PERMISSION_DENIED).
                Log.w("NoteRemoteDataSource", "Firestore listen failed", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val notes = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(NoteDto::class.java)?.copy(id = doc.id)
            }?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(notes)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addNote(note: Note): String {
        val dto = NoteDto.fromNote(note)
        val collection = notesCollection ?: throw IllegalStateException("User not authenticated")
        val docRef = collection.add(dto).await()
        return docRef.id
    }

    suspend fun updateNote(note: Note) {
        val collection = notesCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(note.id).set(NoteDto.fromNote(note)).await()
    }

    suspend fun deleteNote(noteId: String) {
        val collection = notesCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(noteId).delete().await()
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
        val collection = notesCollection ?: throw IllegalStateException("User not authenticated")
        collection.document(id).delete().await()
    }
}
