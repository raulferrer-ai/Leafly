package com.raulferrergarcia.leafly.domain.model

import java.util.UUID

/**
 * Modelo de dominio de una planta.
 *
 * Independiente de Room para mantener el dominio limpio y testeable.
 * Mantiene la misma forma que el modelo de iOS: id, nombre, tipo y fecha.
 */
data class Plant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PlantType? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
