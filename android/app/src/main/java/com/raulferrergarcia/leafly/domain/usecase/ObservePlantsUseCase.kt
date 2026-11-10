package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observa las plantas guardadas, en orden de creación.
 */
class ObservePlantsUseCase(
    private val repository: PlantRepository,
) {
    operator fun invoke(): Flow<List<Plant>> = repository.observePlants()
}
