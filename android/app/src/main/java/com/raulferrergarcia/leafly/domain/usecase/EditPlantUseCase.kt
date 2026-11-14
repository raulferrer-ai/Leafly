package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.repository.PlantRepository

/**
 * Edita una planta existente preservando su identificador y fechas.
 *
 * Solo se actualizan los campos editables: nombre, tipo y foto principal.
 * Se mantiene `id`, `createdAt` y `lastWateredAt` para no perder historial.
 */
open class EditPlantUseCase(
    private val repository: PlantRepository,
) {
    suspend operator fun invoke(
        plantId: String,
        name: String,
        type: PlantType?,
        photoData: ByteArray?,
        createdAt: Long,
        lastWateredAt: Long?,
    ): Plant {
        val updatedPlant = Plant(
            id = plantId,
            name = name,
            type = type,
            createdAt = createdAt,
            lastWateredAt = lastWateredAt,
            photoData = photoData,
        )
        repository.update(updatedPlant)
        return updatedPlant
    }
}
