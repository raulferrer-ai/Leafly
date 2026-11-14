package com.raulferrergarcia.leafly.data.notifications

import com.raulferrergarcia.leafly.domain.model.NotificationPreferences
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.scheduling.WateringAlarmScheduler
import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderPlanner
import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderScheduling
import java.time.ZoneId

/**
 * Mantiene sincronizados los recordatorios de riego con las plantas guardadas.
 *
 * Reglas:
 * - Solo programa recordatorios para plantas que necesitan atención.
 * - Cancela el recordatorio de todas las plantas conocidas antes de reprogramar,
 *   así los cambios de estado (por ejemplo, tras regar) quedan reflejados.
 * - Nunca lanza: si algo del sistema falla, la app sigue funcionando.
 */
class WateringReminderService(
    private val alarmScheduler: WateringAlarmScheduler,
    private val now: () -> Long = System::currentTimeMillis,
    private val zoneId: () -> ZoneId = ZoneId::systemDefault,
) : WateringReminderScheduling {

    override suspend fun sync(
        plants: List<Plant>,
        preferences: NotificationPreferences,
    ) {
        plants.forEach { plant -> cancelSafely(plant.id) }

        val reminders = WateringReminderPlanner.remindersFor(
            plants = plants,
            nowMillis = now(),
            zoneId = zoneId(),
            preferences = preferences,
        )
        reminders.forEach(::scheduleSafely)
    }

    override suspend fun cancelReminder(plantId: String) {
        cancelSafely(plantId)
    }

    private fun scheduleSafely(reminder: WateringReminder) {
        try {
            alarmScheduler.schedule(reminder)
        } catch (_: Exception) {
            // Un fallo al programar no debe romper la app.
        }
    }

    private fun cancelSafely(plantId: String) {
        try {
            alarmScheduler.cancel(plantId)
        } catch (_: Exception) {
            // Un fallo al cancelar no debe romper la app.
        }
    }
}
