package com.raulferrergarcia.leafly.data.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantPhotoProcessorTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val processor = PlantPhotoProcessor(context)

    @Test
    fun downscalesLargePhotosKeepingTheProportion() {
        runBlocking {
            val file = tempFile(jpegBytes(width = 3_000, height = 2_000))

            val processed = processor.process(file)

            assertNotNull(processed)
            val bitmap = BitmapFactory.decodeByteArray(processed!!, 0, processed.size)
            assertTrue(max(bitmap.width, bitmap.height) <= PlantPhotoProcessor.MAX_DIMENSION)
            assertTrue(bitmap.width > bitmap.height)

            file.delete()
        }
    }

    @Test
    fun keepsSmallPhotosIntact() {
        runBlocking {
            val file = tempFile(jpegBytes(width = 320, height = 240))

            val processed = processor.process(file)

            assertNotNull(processed)
            val bitmap = BitmapFactory.decodeByteArray(processed!!, 0, processed.size)
            assertEquals(320, bitmap.width)
            assertEquals(240, bitmap.height)

            file.delete()
        }
    }

    @Test
    fun rejectsInvalidData() {
        runBlocking {
            val file = tempFile(byteArrayOf(1, 2, 3, 4))

            assertNull(processor.process(file))

            file.delete()
        }
    }

    private fun jpegBytes(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.toByteArray()
        }.also { bitmap.recycle() }
    }

    private fun tempFile(bytes: ByteArray): File =
        File.createTempFile("plant-photo-test-", ".jpg", context.cacheDir).apply {
            writeBytes(bytes)
        }
}
