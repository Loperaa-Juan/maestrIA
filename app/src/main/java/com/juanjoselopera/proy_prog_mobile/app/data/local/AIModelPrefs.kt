package com.juanjoselopera.proy_prog_mobile.app.data.local

import android.content.Context
import androidx.core.content.edit

object AIModelPrefs {
    private const val PREFS = "ai_model_prefs"
    private const val KEY_ID = "model_id"
    private const val KEY_NAME = "model_name"

    fun save(context: Context, id: String, name: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_ID, id)
            putString(KEY_NAME, name)
        }

    fun getSelectedId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ID, null)

    fun getSelectedName(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_NAME, null)
}
