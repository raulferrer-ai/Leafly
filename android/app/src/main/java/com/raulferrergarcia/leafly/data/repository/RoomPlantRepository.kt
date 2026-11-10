package com.raulferrergarcia.leafly.data.repository

import com.raulferrergarcia.leafly.data.local.PlantDao
import com.raulferrergarcia.leafly.data.local.toDomain
import com.raulferrergarcia.leafly.data.local.toEntity
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de [PlantRepository] sobre Room.
 */
class RoomPlantRepository(
    private val plantDao: PlantDao,
) : PlantRepository {

    override fun observePlants(): Flow<List<Plant>> =
        plantDao.observePlants().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(plant: Plant) {
        plantDao.insert(plant.toEntity())
    }
}
