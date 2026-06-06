package com.juanjoselopera.proy_prog_mobile.app.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.app.core.connectivity.ConnectivityObserver
import com.juanjoselopera.proy_prog_mobile.app.data.local.AppDatabase
import com.juanjoselopera.proy_prog_mobile.app.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val syncEngine: SyncEngine,
    private val connectivity: ConnectivityObserver,
    private val auth: FirebaseAuth,
    private val database: AppDatabase,
    @ApplicationScope private val scope: CoroutineScope,
) : SyncTrigger {

    private val started = AtomicBoolean(false)

    // Serializa las sincronizaciones: evita que dos sync() concurrentes (p. ej. una
    // ráfaga de escrituras o un write justo al recuperar conexión) se interleaveen.
    private val syncMutex = Mutex()

    // Se llama una vez al arrancar la app.
    fun start() {
        if (!started.compareAndSet(false, true)) return

        // Sincroniza al recuperar conexión (y al inicio, porque observe() emite el estado actual).
        scope.launch {
            connectivity.observe().distinctUntilChanged().collect { online ->
                if (online && auth.currentUser != null) runSync()
            }
        }

        // Reacciona a los cambios de sesión.
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                // Al iniciar sesión, sincroniza para traer los datos del usuario (p. ej. en un
                // dispositivo nuevo): la conectividad no cambia con el login, así que hay que disparar aquí.
                requestSync()
            } else {
                // Al cerrar sesión, limpia los datos locales para no filtrarlos a otra cuenta
                // que inicie sesión en el mismo dispositivo (Room es la fuente de verdad de la UI).
                scope.launch {
                    syncMutex.withLock {
                        withContext(Dispatchers.IO) { database.clearAllTables() }
                    }
                }
            }
        }
    }

    // Lo invocan los repositorios tras una escritura local: si hay red y sesión, sincroniza ya.
    override fun requestSync() {
        scope.launch {
            if (connectivity.isCurrentlyOnline() && auth.currentUser != null) runSync()
        }
    }

    private suspend fun runSync() {
        syncMutex.withLock {
            runCatching { syncEngine.sync() }
                .onFailure { Log.w(TAG, "sync failed", it) }
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
