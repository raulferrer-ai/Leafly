package com.raulferrergarcia.leafly.data.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Prepara las fotos de las plantas para guardarlas de forma local.
 *
 * Reduce el lado más largo a [MAX_DIMENSION] píxeles, corrige la orientación
 * EXIF y comprime en JPEG con calidad [COMPRESSION_QUALITY]. Así la base de
 * datos y la memoria se mantienen ligeras, y la foto se ve nítida en la lista
 * y en el detalle.
 */
class PlantPhotoProcessor(private val context: Context) {

    /**
     * Crea el archivo temporal donde la app de cámara escribirá la captura.
     * Vive en la caché de la app y se borra en cuanto se procesa o se cancela.
     */
    fun createCameraPhotoFile(): File {
        val directory = File(context.cacheDir, CAMERA_DIRECTORY).apply { mkdirs() }
        return File.createTempFile(CAMERA_FILE_PREFIX, CAMERA_FILE_SUFFIX, directory)
    }

    /** URI seguro que se comparte con la app de cámara a través de FileProvider. */
    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Procesa una imagen elegida de la galería (content URI). */
    suspend fun process(uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use(::processStream)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            null
        }
    }

    /** Procesa una foto capturada por la cámara (archivo temporal). */
    suspend fun process(file: File): ByteArray? = withContext(Dispatchers.IO) {
        try {
            file.inputStream().use(::processStream)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            null
        }
    }

    /** Borra un archivo temporal sin propagar errores. */
    fun delete(file: File) {
        runCatching { file.delete() }
    }

    private fun processStream(input: InputStream): ByteArray? {
        val source = input.readBytes()
        if (source.isEmpty()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(source, 0, source.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        }
        val decoded = BitmapFactory.decodeByteArray(source, 0, source.size, options) ?: return null

        val oriented = applyOrientation(decoded, source)
        val scaled = downscale(oriented, MAX_DIMENSION)

        val result = ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, output)
            output.toByteArray()
        }

        if (scaled !== oriented) scaled.recycle()
        if (oriented !== decoded) oriented.recycle()
        decoded.recycle()

        return result
    }

    /** Aplica la orientación EXIF para que la foto no se vea girada. */
    private fun applyOrientation(bitmap: Bitmap, source: ByteArray): Bitmap {
        val orientation = runCatching {
            ExifInterface(ByteArrayInputStream(source)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
        }

        if (matrix.isIdentity) return bitmap

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true,
        )
    }

    /** Reduce la imagen manteniendo la proporción si supera el máximo. */
    private fun downscale(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longestSide = max(bitmap.width, bitmap.height)
        if (longestSide <= maxDimension || longestSide <= 0) return bitmap

        val scale = maxDimension.toFloat() / longestSide
        val width = max(1, (bitmap.width * scale).roundToInt())
        val height = max(1, (bitmap.height * scale).roundToInt())

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * Potencia de dos por la que decodificar, para no cargar en memoria la
     * imagen completa antes de reducirla.
     */
    private fun sampleSizeFor(width: Int, height: Int, maxDimension: Int): Int {
        var sampleSize = 1
        var longestSide = max(width, height)

        while (longestSide / 2 >= maxDimension) {
            longestSide /= 2
            sampleSize *= 2
        }

        return sampleSize
    }

    companion object {
        /** Lado más largo permitido, en píxeles. */
        const val MAX_DIMENSION = 1_400

        /** Calidad JPEG: suficiente para una foto de planta, sin pesar de más. */
        const val COMPRESSION_QUALITY = 80

        private const val CAMERA_DIRECTORY = "plant_photos"
        private const val CAMERA_FILE_PREFIX = "plant-"
        private const val CAMERA_FILE_SUFFIX = ".jpg"
    }
}
