package com.raulferrergarcia.leafly

import android.content.Context
import androidx.room.Room
import com.raulferrergarcia.leafly.data.local.LeaflyDatabase
import com.raulferrergarcia.leafly.data.repository.RoomPlantRepository
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase

/**
 * Contenedor de dependencias de la app.
 *
 * Manual y explícito: para el MVP no necesitamos un framework de inyección.
 */
class AppContainer(context: Context) {

    private val database: LeaflyDatabase = Room.databaseBuilder(
        context.applicationContext,
        LeaflyDatabase::class.java,
        DATABASE_NAME,
    )
        .addMigrations(LeaflyDatabase.MIGRATION_1_2)
        .build()

    private val plantRepository: PlantRepository = RoomPlantRepository(database.plantDao())

    val observePlants: ObservePlantsUseCase = ObservePlantsUseCase(plantRepository)
    val addPlant: AddPlantUseCase = AddPlantUseCase(plantRepository)
    val markPlantWatered: MarkPlantWateredUseCase = MarkPlantWateredUseCase(plantRepository)

    private companion object {
        const val DATABASE_NAME = "leafly.db"
    }
}
