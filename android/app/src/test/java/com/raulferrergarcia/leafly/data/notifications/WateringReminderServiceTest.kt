package com.raulferrergarcia.leafly.data.notifications

import com.raulferrergarcia.leafly.domain.model.NotificationPreferences
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.scheduling.WateringAlarmScheduler
import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderPlanner
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WateringReminderServiceTest {

    private val zone = ZoneId.of("Europe/Madrid")

    private fun millis(day: Int, hour: Int = 12): Long = ZonedDateTime
        .of(2026, 11, day, hour, 0, 0, 0, zone)
        .toInstant()
        .toEpochMilli()

    private fun service(
        scheduler: FakeAlarmScheduler,
        now: Long,
    ): WateringReminderService = WateringReminderService(
        alarmScheduler = scheduler,
        now = { now },
        zoneId = { zone },
    )

    // MARK: - Programación

    @Test
    fun `sync schedules only plants that need attention`() = runTest {
        val scheduler = FakeAlarmScheduler()
        val plants = listOf(
            Plant(name = "Al día", lastWateredAt = millis(day = 9)),
            Plant(name = "Pronto", lastWateredAt = millis(day = 4)),
            Plant(name = "Urgente", lastWateredAt = millis(day = 1)),
        )

        service(scheduler, now = millis(day = 9)).sync(plants, NotificationPreferences())

        assertEquals(listOf("Pronto", "Urgente"), scheduler.scheduled.map { it.plantName })
    }

    @Test
    fun `sync cancels the reminder of every known plant before rescheduling`() = runTest {
        val scheduler = FakeAlarmScheduler()
        val plants = listOf(
            Plant(name = "Al día", lastWateredAt = millis(day = 9)),
            Plant(name = "Urgente", lastWateredAt = millis(day = 1)),
        )

        service(scheduler, now = millis(day = 9)).sync(plants, NotificationPreferences())

        assertEquals(plants.map { it.id }.toSet(), scheduler.cancelled.toSet())
    }

    @Test
    fun `sync without thirsty plants schedules nothing`() = runTest {
        val scheduler = FakeAlarmScheduler()
        val plants = listOf(Plant(name = "Al día", lastWateredAt = millis(day = 9)))

        service(scheduler, now = millis(day = 9)).sync(plants, NotificationPreferences())

        assertTrue(scheduler.scheduled.isEmpty())
        assertEquals(listOf(plants.single().id), scheduler.cancelled)
    }

    @Test
    fun `sync schedules all reminders at the same calm morning slot`() = runTest {
        val scheduler = FakeAlarmScheduler()
        val plants = listOf(
            Plant(name = "Pronto", lastWateredAt = millis(day = 4)),
            Plant(name = "Urgente", lastWateredAt = millis(day = 1)),
        )

        val now = millis(day = 9)
        service(scheduler, now = now).sync(plants, NotificationPreferences())

        val expectedFireAt = WateringReminderPlanner.nextReminderTimeMillis(now, zone)
        assertEquals(
            listOf(expectedFireAt, expectedFireAt),
            scheduler.scheduled.map { it.fireAtMillis },
        )
    }

    // MARK: - Cancelación

    @Test
    fun `cancelReminder cancels only that plant`() = runTest {
        val scheduler = FakeAlarmScheduler()
        val plant = Plant(name = "Urgente", lastWateredAt = millis(day = 1))

        service(scheduler, now = millis(day = 9)).cancelReminder(plant.id)

        assertEquals(listOf(plant.id), scheduler.cancelled)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    // MARK: - Robustez

    @Test
    fun `a scheduler failure does not propagate`() = runTest {
        val scheduler = FakeAlarmScheduler().apply {
            failOnCancel = true
            failOnSchedule = true
        }

        service(scheduler, now = millis(day = 9)).sync(
            listOf(Plant(name = "Urgente", lastWateredAt = millis(day = 1))),
            NotificationPreferences(),
        )

        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `cancelling an unknown plant does not fail`() = runTest {
        val scheduler = FakeAlarmScheduler()

        service(scheduler, now = millis(day = 9)).cancelReminder("planta-que-no-existe")

        assertEquals(listOf("planta-que-no-existe"), scheduler.cancelled)
    }
}

private class FakeAlarmScheduler : WateringAlarmScheduler {

    val scheduled = mutableListOf<WateringReminder>()
    val cancelled = mutableListOf<String>()
    var failOnSchedule: Boolean = false
    var failOnCancel: Boolean = false

    override fun schedule(reminder: WateringReminder) {
        if (failOnSchedule) throw IllegalStateException("No se pudo programar")
        scheduled.add(reminder)
    }

    override fun cancel(plantId: String) {
        if (failOnCancel) throw IllegalStateException("No se pudo cancelar")
        cancelled.add(plantId)
    }
}
