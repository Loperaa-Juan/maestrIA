package com.juanjoselopera.proy_prog_mobile.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Persistencia local de la foto de perfil. Guarda una sola imagen en el
 * almacenamiento interno de la app, reescalada y comprimida para mantenerla
 * pequeña (y disponible offline). Es un objeto plano para que cualquier
 * pantalla (perfil, header) pueda leerla sin pasar por Hilt.
 */
object ProfileImageStore {

    private const val FILE_NAME = "profile_avatar.jpg"
    private const val MAX_SIZE = 512   // px (lado mayor)
    private const val QUALITY = 80     // compresión JPEG

    fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    /** Devuelve el archivo del avatar solo si existe y tiene contenido. */
    fun existing(context: Context): File? =
        file(context).takeIf { it.exists() && it.length() > 0 }

    /** Reescala (<= MAX_SIZE) y guarda los bytes como JPEG. Devuelve el archivo. */
    fun save(context: Context, bytes: ByteArray): File {
        val scaled = decodeScaled(bytes, MAX_SIZE)
        val out = file(context)
        FileOutputStream(out).use { scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
        scaled.recycle()
        return out
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    private fun decodeScaled(bytes: ByteArray, maxSize: Int): Bitmap {
        // Primera pasada: solo dimensiones para calcular inSampleSize y evitar OOM.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        var sample = 1
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        while (largest / sample > maxSize * 2) sample *= 2

        val decoded = BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: throw IllegalArgumentException("No se pudo decodificar la imagen")

        val scale = maxSize.toFloat() / maxOf(decoded.width, decoded.height)
        if (scale >= 1f) return decoded

        val w = (decoded.width * scale).toInt().coerceAtLeast(1)
        val h = (decoded.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(decoded, w, h, true)
            .also { if (it != decoded) decoded.recycle() }
    }
}
