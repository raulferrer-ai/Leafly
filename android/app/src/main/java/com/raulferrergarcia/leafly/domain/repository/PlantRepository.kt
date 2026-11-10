package com.raulferrergarcia.leafly.domain.repository

import com.raulferrergarcia.leafly.domain.model.Plant
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a las plantas guardadas localmente.
 *
 * El dominio no conoce Room: solo este contrato. `observePlants` es la única
 * fuente de verdad que alimenta tanto el routing como la lista de plantas.
 */
interface PlantRepository {
    fun observePlants(): Flow<List<Plant>>
    suspend fun add(plant: Plant)
    /** Guarda los cambios de una planta que ya existía (por ejemplo, un riego). */
    suspend fun update(plant: Plant)
}

/** La planta que se quería actualizar ya no existe. */
class PlantNotFoundException(plantId: String) : Exception("No existe la planta $plantId")
