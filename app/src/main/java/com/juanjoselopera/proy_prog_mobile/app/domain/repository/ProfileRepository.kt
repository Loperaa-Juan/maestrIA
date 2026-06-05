package com.juanjoselopera.proy_prog_mobile.app.domain.repository

import com.juanjoselopera.proy_prog_mobile.app.util.Resource

interface ProfileRepository {

    /** Actualiza el nombre visible en Firebase Auth y lo cachea localmente. */
    suspend fun updateDisplayName(name: String): Resource<Unit>

    /**
     * Guarda la foto en local, la sube a Firebase Storage y fija la URL como
     * photoUrl en Firebase Auth. Devuelve la URL de descarga.
     */
    suspend fun updateProfilePhoto(bytes: ByteArray): Resource<String>
}
