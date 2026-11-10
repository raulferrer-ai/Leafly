package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.repository.PlantRepository

/**
 * Marca una planta como regada ahora mismo.
 *
 * La regla vive en el dominio y el reloj se inyecta para poder testearla:
 * solo cambia `lastWateredAt`, el resto de la planta se conserva.
 */
class MarkPlantWateredUseCase(
    private val repository: PlantRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(plant: Plant): Plant {
        val wateredPlant = plant.copy(lastWateredAt = now())
        repository.update(wateredPlant)
        return wateredPlant
    }
}
