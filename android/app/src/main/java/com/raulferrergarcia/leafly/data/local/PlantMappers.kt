package com.raulferrergarcia.leafly.data.local

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType

/** Traduce una entidad persistente al modelo de dominio. */
fun PlantEntity.toDomain(): Plant = Plant(
    id = id,
    name = name,
    type = typeKey?.let(PlantType::fromKey),
    createdAt = createdAt,
    lastWateredAt = lastWateredAt,
)

/** Traduce un modelo de dominio a su representación persistente. */
fun Plant.toEntity(): PlantEntity = PlantEntity(
    id = id,
    name = name,
    typeKey = type?.key,
    createdAt = createdAt,
    lastWateredAt = lastWateredAt,
)
