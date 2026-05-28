package com.juanjoselopera.proy_prog_mobile.app.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    operator fun invoke(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al enviar el correo"))
        }
    }
}
