package com.juanjoselopera.proy_prog_mobile.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.juanjoselopera.proy_prog_mobile.app.data.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MaestrIAApplication : Application() {

    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("maestria_prefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        // Empieza a observar la conectividad y sincroniza al recuperar red.
        syncManager.start()
    }
}
