package com.raulferrergarcia.leafly.data.local

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.model.WateringLogEntry

/** Traduce una entidad persistente al modelo de dominio. */
fun PlantEntity.toDomain(): Plant = Plant(
    id = id,
    name = name,
    type = typeKey?.let(PlantType::fromKey),
    createdAt = createdAt,
    lastWateredAt = lastWateredAt,
    photoData = photoData,
)

/** Traduce un modelo de dominio a su representación persistente. */
fun Plant.toEntity(): PlantEntity = PlantEntity(
    id = id,
    name = name,
    typeKey = type?.key,
    createdAt = createdAt,
    lastWateredAt = lastWateredAt,
    photoData = photoData,
)

// -----------------------------------------------------------------------
// Historial de fotos
// -----------------------------------------------------------------------

/** Traduce una entidad persistente al modelo de dominio. */
fun PlantPhotoEntity.toDomain(): PlantPhoto = PlantPhoto(
    id = id,
    plantId = plantId,
    addedAt = addedAt,
    data = photoData,
)

/** Traduce un modelo de dominio a su representación persistente. */
fun PlantPhoto.toEntity(plantId: String): PlantPhotoEntity = PlantPhotoEntity(
    id = id,
    plantId = plantId,
    addedAt = addedAt,
    photoData = data,
)

// -----------------------------------------------------------------------
// Historial de riegos
// -----------------------------------------------------------------------

/** Traduce una entidad persistente al modelo de dominio. */
fun WateringLogEntryEntity.toDomain(): WateringLogEntry = WateringLogEntry(
    id = id,
    plantId = plantId,
    wateredAt = wateredAt,
)

/** Traduce un modelo de dominio a su representación persistente. */
fun WateringLogEntry.toEntity(plantId: String): WateringLogEntryEntity = WateringLogEntryEntity(
    id = id,
    plantId = plantId,
    wateredAt = wateredAt,
)
