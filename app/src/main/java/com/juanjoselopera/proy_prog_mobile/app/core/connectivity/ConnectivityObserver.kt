package com.juanjoselopera.proy_prog_mobile.app.core.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    // Emite el estado actual al suscribirse y luego cada cambio (true = online).
    fun observe(): Flow<Boolean>
    fun isCurrentlyOnline(): Boolean
}
