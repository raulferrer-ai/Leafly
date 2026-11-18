package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.model.WateringLogEntry
import com.raulferrergarcia.leafly.domain.repository.PlantRepository

/**
 * Añade una entrada al historial de riegos de una planta.
 *
 * Se invoca tras registrar el riego (actualizar `lastWateredAt`) para que el
 * historial guarde cuándo se regó la planta. La regla vive en el dominio y el
 * reloj se inyecta para poder testearla.
 */
class AddWateringLogEntryUseCase(
    private val repository: PlantRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(plantId: String): WateringLogEntry {
        val entry = WateringLogEntry(plantId = plantId, wateredAt = now())
        repository.addWateringLogEntry(entry)
        return entry
    }
}
