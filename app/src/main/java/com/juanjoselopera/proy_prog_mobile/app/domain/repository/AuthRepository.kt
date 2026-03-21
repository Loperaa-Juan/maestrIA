package com.juanjoselopera.proy_prog_mobile.app.domain.repository

import com.juanjoselopera.proy_prog_mobile.app.util.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Boolean

    suspend fun signup(email: String, password: String): Boolean
}