package com.raulferrergarcia.leafly.domain.model

import java.util.UUID

/**
 * Una entrada del historial de riegos de una planta.
 *
 * Cada vez que el usuario marca una planta como regada, se añade una
 * entrada aquí con la fecha del riego. El historial crece con el tiempo y
 * se muestra del más reciente al más antiguo.
 */
data class WateringLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val plantId: String,
    /** Momento en el que se registró el riego (epoch millis). */
    val wateredAt: Long = System.currentTimeMillis(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WateringLogEntry) return false

        return id == other.id &&
            plantId == other.plantId &&
            wateredAt == other.wateredAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + plantId.hashCode()
        result = 31 * result + wateredAt.hashCode()
        return result
    }

    override fun toString(): String =
        "WateringLogEntry(id=$id, plantId=$plantId, wateredAt=$wateredAt)"
}
