package com.raulferrergarcia.leafly.domain.scheduling

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WateringReminderPlannerTest {

    private val zone = ZoneId.of("Europe/Madrid")

    private fun millis(
        day: Int,
        hour: Int = 12,
        minute: Int = 0,
        month: Int = 11,
        year: Int = 2026,
    ): Long = ZonedDateTime
        .of(year, month, day, hour, minute, 0, 0, zone)
        .toInstant()
        .toEpochMilli()

    // MARK: - Qué plantas generan recordatorio

    @Test
    fun `a healthy plant does not create a reminder`() {
        val plant = Plant(name = "Monstera", lastWateredAt = millis(day = 7))

        val reminders = WateringReminderPlanner.remindersFor(
            plants = listOf(plant),
            nowMillis = millis(day = 7),
            zoneId = zone,
        )

        assertTrue(reminders.isEmpty())
    }

    @Test
    fun `a plant that needs water soon creates a reminder`() {
        val plant = Plant(name = "Ficus", lastWateredAt = millis(day = 1))

        val reminders = WateringReminderPlanner.remindersFor(
            plants = listOf(plant),
            nowMillis = millis(day = 6),
            zoneId = zone,
        )

        assertEquals(1, reminders.size)
        assertEquals(plant.id, reminders.single().plantId)
        assertEquals("Ficus", reminders.single().plantName)
        assertEquals(WateringStatus.NEEDS_WATER_SOON, reminders.single().status)
    }

    @Test
    fun `a plant that needs water now creates a reminder`() {
        val plant = Plant(name = "Monstera", lastWateredAt = millis(day = 1))

        val reminders = WateringReminderPlanner.remindersFor(
            plants = listOf(plant),
            nowMillis = millis(day = 9),
            zoneId = zone,
        )

        assertEquals(1, reminders.size)
        assertEquals(WateringStatus.NEEDS_WATER_NOW, reminders.single().status)
    }

    @Test
    fun `only plants that need attention are included`() {
        val plants = listOf(
            Plant(name = "Al día", lastWateredAt = millis(day = 9)),
            Plant(name = "Pronto", lastWateredAt = millis(day = 3)),
            Plant(name = "Urgente", lastWateredAt = millis(day = 1)),
        )

        val reminders = WateringReminderPlanner.remindersFor(
            plants = plants,
            nowMillis = millis(day = 9),
            zoneId = zone,
        )

        assertEquals(listOf("Pronto", "Urgente"), reminders.map { it.plantName })
        assertEquals(
            listOf(WateringStatus.NEEDS_WATER_SOON, WateringStatus.NEEDS_WATER_NOW),
            reminders.map { it.status },
        )
    }

    @Test
    fun `each reminder has a stable identifier for its plant`() {
        val plant = Plant(name = "Potos", lastWateredAt = millis(day = 1))

        val first = WateringReminderPlanner.remindersFor(
            plants = listOf(plant),
            nowMillis = millis(day = 8),
            zoneId = zone,
        )
        val second = WateringReminderPlanner.remindersFor(
            plants = listOf(plant),
            nowMillis = millis(day = 9),
            zoneId = zone,
        )

        assertEquals(first.single().notificationId, second.single().notificationId)
        assertEquals(WateringReminder.stableId(plant.id), first.single().notificationId)
    }

    // MARK: - Momento del recordatorio

    @Test
    fun `the reminder uses today at ten when there is enough time`() {
        val fireAt = WateringReminderPlanner.nextReminderTimeMillis(
            nowMillis = millis(day = 8, hour = 8),
            zoneId = zone,
        )

        assertEquals(millis(day = 8, hour = 10), fireAt)
    }

    @Test
    fun `the reminder moves to tomorrow when the slot has passed`() {
        val fireAt = WateringReminderPlanner.nextReminderTimeMillis(
            nowMillis = millis(day = 8, hour = 11),
            zoneId = zone,
        )

        assertEquals(millis(day = 9, hour = 10), fireAt)
    }

    @Test
    fun `the reminder keeps at least one hour of margin`() {
        val fireAt = WateringReminderPlanner.nextReminderTimeMillis(
            nowMillis = millis(day = 8, hour = 9, minute = 30),
            zoneId = zone,
        )

        assertEquals(millis(day = 9, hour = 10), fireAt)
    }

    @Test
    fun `the reminder is always in the future`() {
        val now = millis(day = 8, hour = 10)

        val fireAt = WateringReminderPlanner.nextReminderTimeMillis(
            nowMillis = now,
            zoneId = zone,
        )

        assertTrue(fireAt > now)
        assertTrue(fireAt - now >= WateringReminderPlanner.MINIMUM_LEAD_MILLIS)
    }
}
