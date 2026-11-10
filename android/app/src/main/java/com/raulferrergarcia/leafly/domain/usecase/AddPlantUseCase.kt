package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.repository.PlantRepository

/**
 * Crea y guarda una planta nueva.
 *
 * La validación del nombre vive en el ViewModel; aquí solo se construye el
 * modelo de dominio y se persiste. La foto es opcional y llega ya procesada.
 */
class AddPlantUseCase(
    private val repository: PlantRepository,
) {
    suspend operator fun invoke(
        name: String,
        type: PlantType?,
        photoData: ByteArray? = null,
    ): Plant {
        val plant = Plant(name = name, type = type, photoData = photoData)
        repository.add(plant)
        return plant
    }
}
