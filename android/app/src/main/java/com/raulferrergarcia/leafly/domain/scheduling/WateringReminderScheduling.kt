package com.raulferrergarcia.leafly.domain.scheduling

import com.raulferrergarcia.leafly.domain.model.NotificationPreferences
import com.raulferrergarcia.leafly.domain.model.Plant

/**
 * Contrato para mantener al día los recordatorios de riego.
 *
 * Los ViewModels solo conocen este contrato; las alarmas y las notificaciones
 * quedan detrás de `WateringAlarmScheduler`.
 */
interface WateringReminderScheduling {
    /**
     * Recalcula los recordatorios a partir del estado actual de las plantas.
     *
     * - Parameter preferences: Preferencias de hora del usuario.
     *
     * No debe lanzar: si no hay permiso de notificaciones, simplemente no se
     * mostrará nada y la app seguirá funcionando con normalidad.
     */
    suspend fun sync(plants: List<Plant>, preferences: NotificationPreferences)

    /** Elimina el recordatorio pendiente de una planta concreta. */
    suspend fun cancelReminder(plantId: String)
}
