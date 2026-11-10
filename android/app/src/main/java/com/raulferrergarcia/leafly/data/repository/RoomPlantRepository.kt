package com.raulferrergarcia.leafly.data.repository

import com.raulferrergarcia.leafly.data.local.PlantDao
import com.raulferrergarcia.leafly.data.local.toDomain
import com.raulferrergarcia.leafly.data.local.toEntity
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import com.raulferrergarcia.leafly.domain.repository.PlantNotFoundException
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

    override suspend fun update(plant: Plant) {
        val updatedRows = plantDao.update(plant.toEntity())
        if (updatedRows == 0) {
            throw PlantNotFoundException(plant.id)
        }
    }

    // ------------------------------------------------------------------
    // Historial de fotos
    // ------------------------------------------------------------------

    override fun observePhotos(plantId: String): Flow<List<PlantPhoto>> =
        plantDao.observePhotos(plantId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addPhoto(photo: PlantPhoto) {
        // Verificamos que la planta existe antes de insertar la foto.
        val plantEntity = plantDao.findPlantById(photo.plantId)
            ?: throw PlantNotFoundException(photo.plantId)

        plantDao.insertPhoto(photo.toEntity(plantId = photo.plantId))

        // La foto más reciente pasa a ser la imagen principal de la planta.
        val plantWithPhoto = plantEntity.toDomain().copy(photoData = photo.data)
        plantDao.update(plantWithPhoto.toEntity())
    }
}
