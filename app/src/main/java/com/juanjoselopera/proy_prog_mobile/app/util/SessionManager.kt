package com.juanjoselopera.proy_prog_mobile.app.util

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Gestiona la sesión del usuario usando SharedPreferences.
// Permite saber si hay un usuario logueado sin consultar Firebase
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)

    // Solo lectura desde fuera: el estado de sesión no se modifica directamente
    val isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    val userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)

    val userId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    // Se llama al hacer login o registro exitoso para persistir la sesión
    fun saveSession(uid: String, email: String) {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, uid)
            putString(KEY_USER_EMAIL, email)
        }
    }

    // Se llama al cerrar sesión: borra todo el archivo de preferencias de sesión
    fun clearSession() = prefs.edit { clear() }

    companion object {
        private const val SESSION_PREFS = "maestria_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
    }
}
