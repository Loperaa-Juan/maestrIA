package com.juanjoselopera.proy_prog_mobile.app.domain.usecase

import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AuthRepository
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FirebaseLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        val loginState = authRepository.login(email, password)
        if (loginState) {
            emit(Resource.Success(true))
        } else {
            emit(Resource.Error("Error"))
        }
    }
}