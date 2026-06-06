package com.juanjoselopera.proy_prog_mobile.app.core.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            // onCapabilitiesChanged es la única señal fiable de "internet real": onAvailable
            // se dispara cuando el enlace está arriba, antes de que Android valide la conexión
            // (NET_CAPABILITY_VALIDATED). Emitir true aquí garantiza que, al terminar la
            // validación tras reconectar, el flujo entregue el flanco false->true que dispara el sync.
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.hasInternet())
            }
            // Emitimos false directamente: no consultamos isCurrentlyOnline() porque en el
            // instante del callback activeNetwork/capabilities aún puede reportar la red caída
            // como válida (el estado global no se actualiza atómicamente con el callback), y un
            // false perdido haría que distinctUntilChanged descarte el true de la reconexión.
            override fun onLost(network: Network) { trySend(false) }
            override fun onUnavailable() { trySend(false) }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        // Estado inicial para que los suscriptores reciban el valor actual de inmediato.
        trySend(isCurrentlyOnline())
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged().conflate()

    override fun isCurrentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasInternet()
    }

    private fun NetworkCapabilities.hasInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
