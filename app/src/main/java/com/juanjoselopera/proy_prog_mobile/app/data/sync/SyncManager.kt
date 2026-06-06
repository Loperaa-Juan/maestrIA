package com.juanjoselopera.proy_prog_mobile.app.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.app.core.connectivity.ConnectivityObserver
import com.juanjoselopera.proy_prog_mobile.app.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val syncEngine: SyncEngine,
    private val connectivity: ConnectivityObserver,
    private val auth: FirebaseAuth,
    @ApplicationScope private val scope: CoroutineScope,
) : SyncTrigger {

    private val started = java.util.concurrent.atomic.AtomicBoolean(false)

    // Se llama una vez al arrancar la app: sincroniza al recuperar conexión
    // (y al inicio, porque observe() emite el estado actual).
    fun start() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            connectivity.observe().distinctUntilChanged().collect { online ->
                if (online && auth.currentUser != null) {
                    runCatching { syncEngine.sync() }
                        .onFailure { Log.w(TAG, "sync failed", it) }
                }
            }
        }
    }

    // Lo invocan los repositorios tras una escritura local: si hay red, sincroniza ya.
    override fun requestSync() {
        scope.launch {
            if (connectivity.isCurrentlyOnline() && auth.currentUser != null) {
                runCatching { syncEngine.sync() }
                    .onFailure { Log.w(TAG, "sync failed", it) }
            }
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
