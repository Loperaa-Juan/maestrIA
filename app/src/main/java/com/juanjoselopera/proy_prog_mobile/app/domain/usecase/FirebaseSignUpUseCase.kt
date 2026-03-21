package com.juanjoselopera.proy_prog_mobile.app.domain.usecase

import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AuthRepository
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FirebaseSignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<Boolean>> =
        flow {
        emit(Resource.Loading)
        val signupSuccessfully = authRepository.signup(email, password)
        if (signupSuccessfully) {
            emit(Resource.Success(true))
        } else {
            emit(Resource.Error("Error en el servidor o datos inválidos"))
        }
    }
}