package com.raulferrergarcia.leafly.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    @Query("SELECT * FROM plants ORDER BY createdAt ASC")
    fun observePlants(): Flow<List<PlantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: PlantEntity)

    /** Devuelve cuántas filas se actualizaron (0 si la planta ya no existe). */
    @Update
    suspend fun update(plant: PlantEntity): Int
}
