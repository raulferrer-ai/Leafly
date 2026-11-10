package com.raulferrergarcia.leafly.domain.scheduling

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import com.raulferrergarcia.leafly.domain.model.wateringStatus
import java.time.Instant
import java.time.ZoneId

/**
 * Decide qué recordatorios deben existir a partir del estado de riego.
 *
 * Es lógica pura: recibe plantas y un instante, y devuelve los recordatorios
 * pendientes. Así se puede probar sin tocar alarmas ni notificaciones.
 *
 * Sigue el mismo criterio que iOS:
 * - Solo `NEEDS_WATER_SOON` y `NEEDS_WATER_NOW` generan recordatorio.
 * - Se entrega en el próximo hueco de las 10:00 locales con al menos una hora
 *   de margen; si ese hueco ya pasó, mañana a la misma hora.
 */
object WateringReminderPlanner {

    /** Hora local a la que se entregan los recordatorios. */
    const val REMINDER_HOUR = 10

    /** Margen mínimo para no enviar una notificación casi inmediata. */
    const val MINIMUM_LEAD_MILLIS: Long = 60L * 60L * 1000L

    /** Plantas "Riega pronto" o "Necesita agua" generan recordatorio. */
    fun remindersFor(
        plants: List<Plant>,
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<WateringReminder> {
        val fireAtMillis = nextReminderTimeMillis(nowMillis, zoneId)

        return plants.mapNotNull { plant ->
            val status = plant.wateringStatus(nowMillis, zoneId)
            if (status == WateringStatus.HEALTHY) {
                null
            } else {
                WateringReminder(
                    plantId = plant.id,
                    plantName = plant.name,
                    status = status,
                    fireAtMillis = fireAtMillis,
                )
            }
        }
    }

    /** Próxima mañana tranquila (10:00), con al menos una hora de margen. */
    fun nextReminderTimeMillis(
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val todaySlot = now
            .toLocalDate()
            .atTime(REMINDER_HOUR, 0)
            .atZone(zoneId)

        val todayMillis = todaySlot.toInstant().toEpochMilli()
        return if (todayMillis - nowMillis >= MINIMUM_LEAD_MILLIS) {
            todayMillis
        } else {
            todaySlot.plusDays(1).toInstant().toEpochMilli()
        }
    }
}
