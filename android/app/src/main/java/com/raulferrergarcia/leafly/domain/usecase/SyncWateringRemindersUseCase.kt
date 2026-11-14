package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.model.NotificationPreferences
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderScheduling

/**
 * Mantiene los recordatorios de riego sincronizados con las plantas guardadas.
 *
 * Se llama cada vez que la lista de plantas cambia, para que solo queden
 * programadas las que necesitan atención.
 */
class SyncWateringRemindersUseCase(
    private val reminders: WateringReminderScheduling,
) {
    suspend operator fun invoke(
        plants: List<Plant>,
        preferences: NotificationPreferences = NotificationPreferences.Default,
    ) {
        reminders.sync(plants, preferences)
    }
}
