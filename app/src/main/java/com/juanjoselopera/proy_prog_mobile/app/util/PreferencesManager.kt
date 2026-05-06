package com.juanjoselopera.proy_prog_mobile.app.util

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Envuelve SharedPreferences para guardar configuraciones generales de la app
// @Singleton asegura que solo exista una instancia en toda la aplicación
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Las propiedades con get/set leen y escriben directamente en SharedPreferences
    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_DARK_MODE, value) }

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "es") ?: "es"
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value) }

    fun clear() = prefs.edit { clear() }

    companion object {
        private const val PREFS_NAME = "maestria_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
    }
}
