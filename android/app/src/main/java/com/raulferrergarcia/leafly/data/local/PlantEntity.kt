package com.raulferrergarcia.leafly.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representación persistente de una planta.
 *
 * El tipo se guarda como texto para que el esquema sea estable aunque el enum
 * evolucione. La traducción a dominio vive en [PlantMappers].
 */
@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val typeKey: String?,
    val createdAt: Long,
)
