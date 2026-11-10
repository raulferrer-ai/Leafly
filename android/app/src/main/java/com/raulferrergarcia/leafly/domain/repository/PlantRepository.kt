package com.raulferrergarcia.leafly.domain.repository

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
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

    // ------------------------------------------------------------------
    // Historial de fotos
    // ------------------------------------------------------------------

    /** Fotos de una planta, de más reciente a más antigua. */
    fun observePhotos(plantId: String): Flow<List<PlantPhoto>>

    /**
     * Añade una foto al historial de una planta y la deja como foto principal.
     * Lanza [PlantNotFoundException] si la planta no existe.
     */
    suspend fun addPhoto(photo: PlantPhoto)
}

/** La planta que se quería actualizar ya no existe. */
class PlantNotFoundException(plantId: String) : Exception("No existe la planta $plantId")
