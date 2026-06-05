package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.ProfileRepository
import com.juanjoselopera.proy_prog_mobile.app.util.PreferencesManager
import com.juanjoselopera.proy_prog_mobile.app.util.ProfileImageStore
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val prefs: PreferencesManager
) : ProfileRepository {

    override suspend fun updateDisplayName(name: String): Resource<Unit> {
        val user = auth.currentUser ?: return Resource.Error("No hay sesión activa")
        return try {
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(request).await()
            prefs.profileName = name
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "No se pudo actualizar el nombre")
        }
    }

    override suspend fun updateProfilePhoto(bytes: ByteArray): Resource<String> {
        val user = auth.currentUser ?: return Resource.Error("No hay sesión activa")
        return try {
            // 1) Persistencia local (reescalada): disponible offline al instante.
            val localFile = ProfileImageStore.save(context, bytes)
            val data = localFile.readBytes()
            if (data.isEmpty()) return Resource.Error("La imagen está vacía")

            // 2) Subir y obtener la URL como CONTINUACIÓN de la subida, desde la
            //    referencia del propio resultado. Esto evita el error
            //    "Object does not exist at location" que aparece al pedir
            //    getDownloadUrl() en una llamada suelta antes de que el objeto
            //    sea consultable.
            val ref = storage.reference.child("profile_photos/${user.uid}.jpg")
            val downloadUrl = ref.putBytes(data)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception ?: Exception("Falló la subida")
                    task.result.storage.downloadUrl
                }
                .await()

            // 3) Reflejar la URL en el perfil de Auth.
            val request = UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUrl)
                .build()
            user.updateProfile(request).await()

            // 4) Cachear la ruta local.
            prefs.profilePhotoPath = localFile.absolutePath

            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(mapUploadError(e))
        }
    }

    private fun mapUploadError(e: Exception): String {
        val msg = e.localizedMessage ?: "No se pudo subir la foto"
        return if (msg.contains("not exist", ignoreCase = true)) {
            "No se pudo subir la foto. Verifica que Firebase Storage esté " +
                "habilitado y que sus reglas permitan la escritura."
        } else {
            msg
        }
    }
}
