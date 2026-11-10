package com.raulferrergarcia.leafly.features.plants

import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantPhotoDateLabelTest {

    private val zoneId = ZoneId.of("UTC")

    private fun millis(year: Int, month: Int, day: Int): Long {
        return Instant.parse("$year-${String.format("%02d", month)}-${String.format("%02d", day)}T12:00:00Z")
            .toEpochMilli()
    }

    @Test
    fun `today is hoy`() {
        val now = millis(2026, 11, 10)
        assertEquals("Hoy", PlantPhotoDateLabel(
            PlantPhoto(plantId = "p1", addedAt = now, data = byteArrayOf()),
            nowMillis = now,
        ))
    }

    @Test
    fun `yesterday is ayer`() {
        val now = millis(2026, 11, 10)
        val yesterday = millis(2026, 11, 9)
        assertEquals("Ayer", PlantPhotoDateLabel(
            PlantPhoto(plantId = "p1", addedAt = yesterday, data = byteArrayOf()),
            nowMillis = now,
        ))
    }

    @Test
    fun `older photo in current year shows day and month without year`() {
        val now = millis(2026, 11, 10)
        val older = millis(2026, 3, 4)
        val label = PlantPhotoDateLabel(
            PlantPhoto(plantId = "p1", addedAt = older, data = byteArrayOf()),
            nowMillis = now,
        )
        assertTrue(label.contains("4"))
        assertTrue(label.toLowerCase().contains("mar"))
        assertFalse(label.contains("2026"))
    }

    @Test
    fun `photo from another year shows the year`() {
        val now = millis(2026, 11, 10)
        val older = millis(2025, 12, 31)
        val label = PlantPhotoDateLabel(
            PlantPhoto(plantId = "p1", addedAt = older, data = byteArrayOf()),
            nowMillis = now,
        )
        assertTrue(label.contains("2025"))
    }
}
