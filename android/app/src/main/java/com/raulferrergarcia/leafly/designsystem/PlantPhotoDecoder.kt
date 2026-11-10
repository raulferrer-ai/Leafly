package com.raulferrergarcia.leafly.designsystem

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DEFAULT_PHOTO_SIZE = 256

/**
 * Decodifica una foto de planta para mostrarla en la UI.
 *
 * Las fotos ya vienen procesadas y ligeras (máximo 1400 px), pero para un
 * avatar no hace falta tanta resolución: se decodifica una miniatura y se
 * cachea para que el scroll de la lista sea fluido.
 */
@Composable
fun rememberPlantPhoto(
    photoData: ByteArray?,
    maxSize: Int = DEFAULT_PHOTO_SIZE,
): ImageBitmap? {
    var image by remember(photoData, maxSize) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(photoData, maxSize) {
        image = if (photoData == null) {
            null
        } else {
            withContext(Dispatchers.Default) {
                PlantPhotoDecoder.decode(photoData, maxSize)
            }
        }
    }

    return image
}

/** Decodificador compartido con una caché pequeña en memoria. */
object PlantPhotoDecoder {

    private val cache = object : LruCache<ByteArray, ImageBitmap>(CACHE_BYTES) {
        override fun sizeOf(key: ByteArray, value: ImageBitmap): Int =
            value.width * value.height * 4
    }

    fun decode(data: ByteArray, maxSize: Int): ImageBitmap? {
        cache.get(data)?.let { return it }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSize)
        }
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options) ?: return null

        return bitmap.asImageBitmap().also { image ->
            cache.put(data, image)
        }
    }

    private fun sampleSizeFor(width: Int, height: Int, maxSize: Int): Int {
        var sampleSize = 1
        var longestSide = max(width, height)

        while (longestSide / 2 >= maxSize) {
            longestSide /= 2
            sampleSize *= 2
        }

        return sampleSize
    }

    private const val CACHE_BYTES = 8 * 1024 * 1024
}
