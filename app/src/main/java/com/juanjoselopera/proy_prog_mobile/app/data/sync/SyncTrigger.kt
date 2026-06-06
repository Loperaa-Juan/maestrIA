package com.juanjoselopera.proy_prog_mobile.app.data.sync

// Permite a los repositorios solicitar una sincronización tras una escritura local.
interface SyncTrigger {
    fun requestSync()
}
