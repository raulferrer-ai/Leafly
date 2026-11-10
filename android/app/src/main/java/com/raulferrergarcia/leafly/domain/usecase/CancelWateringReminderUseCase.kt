package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderScheduling

/**
 * Cancela el recordatorio pendiente de una planta.
 *
 * Se usa al marcarla como regada: su estado pasa a "Al día" y el aviso deja de
 * tener sentido.
 */
class CancelWateringReminderUseCase(
    private val reminders: WateringReminderScheduling,
) {
    suspend operator fun invoke(plantId: String) {
        reminders.cancelReminder(plantId)
    }
}
