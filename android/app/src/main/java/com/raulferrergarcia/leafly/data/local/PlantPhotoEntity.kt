package com.raulferrergarcia.leafly.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representación persistente de una foto del historial.
 *
 * Cada foto pertenece a una planta. Al borrar la planta, sus fotos se borran
 * con ella (CASCADE). La imagen se guarda como BLOB ya procesado (JPEG ligero).
 */
@Entity(
    tableName = "plant_photos",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["plantId"]), Index(value = ["addedAt"])],
)
data class PlantPhotoEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    /** Momento en el que se añadió la foto al historial (epoch millis). */
    val addedAt: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val photoData: ByteArray,
)
