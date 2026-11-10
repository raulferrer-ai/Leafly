package com.raulferrergarcia.leafly.domain.model

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class WateringStatusTest {

    private val zone: ZoneId = ZoneId.of("Europe/Madrid")

    private fun millis(day: Int, hour: Int = 12): Long =
        LocalDate.of(2026, 11, day)
            .atTime(hour, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

    @Test
    fun `freshly watered plant is healthy`() {
        val plant = Plant(name = "Monstera", lastWateredAt = millis(day = 7))

        assertEquals(
            WateringStatus.HEALTHY,
            plant.wateringStatus(nowMillis = millis(day = 7), zoneId = zone),
        )
    }

    @Test
    fun `the last day before the soon threshold is healthy`() {
        val plant = Plant(name = "Monstera", lastWateredAt = millis(day = 1))

        assertEquals(
            WateringStatus.HEALTHY,
            plant.wateringStatus(nowMillis = millis(day = 5), zoneId = zone),
        )
    }

    @Test
    fun `the soon threshold starts at five days`() {
        val plant = Plant(name = "Monstera", lastWateredAt = millis(day = 1))

        assertEquals(
            WateringStatus.NEEDS_WATER_SOON,
            plant.wateringStatus(nowMillis = millis(day = 6), zoneId = zone),
        )
    }

    @Test
    fun `the last day before the urgent threshold still needs water soon`() {
        val plant = Plant(name = "Monstera", lastWateredAt = millis(day = 1))

        assertEquals(
            WateringStatus.NEEDS_WATER_SOON,
            plant.wateringStatus(nowMillis = millis(day = 7), zoneId = zone),
        )
    }

    @Test
    fun `the urgent threshold starts at seven days`() {
        val plant = Plant(name = "Monstera", lastWateredAt = millis(day = 1))

        assertEquals(
            WateringStatus.NEEDS_WATER_NOW,
            plant.wateringStatus(nowMillis = millis(day = 8), zoneId = zone),
        )
    }

    @Test
    fun `negative days are treated as healthy`() {
        assertEquals(WateringStatus.HEALTHY, WateringStatus.fromDays(-3))
    }

    @Test
    fun `a plant without watering uses its creation date`() {
        val plant = Plant(name = "Nueva", createdAt = millis(day = 1))

        assertEquals(millis(day = 1), plant.wateringReferenceMillis)
        assertEquals(
            WateringStatus.NEEDS_WATER_NOW,
            plant.wateringStatus(nowMillis = millis(day = 9), zoneId = zone),
        )
    }

    @Test
    fun `last watered date takes precedence over creation date`() {
        val plant = Plant(
            name = "Ficus",
            createdAt = millis(day = 1),
            lastWateredAt = millis(day = 8),
        )

        assertEquals(millis(day = 8), plant.wateringReferenceMillis)
        assertEquals(
            WateringStatus.HEALTHY,
            plant.wateringStatus(nowMillis = millis(day = 9), zoneId = zone),
        )
    }

    @Test
    fun `time of day does not affect the number of days`() {
        val plant = Plant(name = "Potos", lastWateredAt = millis(day = 1, hour = 23))

        assertEquals(
            2,
            plant.daysSinceWatering(nowMillis = millis(day = 3, hour = 1), zoneId = zone),
        )
    }
}
