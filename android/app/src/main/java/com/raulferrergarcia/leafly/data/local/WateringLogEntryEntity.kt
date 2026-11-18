package com.raulferrergarcia.leafly.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representación persistente de una entrada del historial de riegos.
 *
 * Cada entrada pertenece a una planta. Al borrar la planta, sus entradas
 * se borran con ella (CASCADE). La fecha se guarda como epoch millis.
 */
@Entity(
    tableName = "watering_log_entries",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["plantId"]), Index(value = ["wateredAt"])],
)
data class WateringLogEntryEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    /** Momento en el que se registró el riego (epoch millis). */
    val wateredAt: Long,
)
