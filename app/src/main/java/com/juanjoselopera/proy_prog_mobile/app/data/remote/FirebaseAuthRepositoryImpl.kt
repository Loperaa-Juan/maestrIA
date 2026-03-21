package com.juanjoselopera.proy_prog_mobile.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): Boolean {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            true // Si llega aquí, el login fue exitoso
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signup(email: String, password: String): Boolean {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            true // Si llega aquí, el usuario se creó correctamente
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAuth", "Error: ${e.message}")
            false
        }
    }
}