package com.raulferrergarcia.leafly.data.local

import androidx.room.Dao
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    @Query("SELECT * FROM plants ORDER BY createdAt ASC")
    fun observePlants(): Flow<List<PlantEntity>>

    /** Lectura única de una planta por su ID. */
    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun findPlantById(id: String): PlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: PlantEntity)

    /** Devuelve cuántas filas se actualizaron (0 si la planta ya no existe). */
    @Update
    suspend fun update(plant: PlantEntity): Int

    // ------------------------------------------------------------------
    // Historial de fotos
    // ------------------------------------------------------------------

    /** Fotos de una planta, de más reciente a más antigua. */
    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY addedAt DESC")
    fun observePhotos(plantId: String): Flow<List<PlantPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PlantPhotoEntity)

    /** Borra todas las fotos de una planta (útil si se borra la planta manualmente). */
    @Query("DELETE FROM plant_photos WHERE plantId = :plantId")
    suspend fun deletePhotosOfPlant(plantId: String)
}
