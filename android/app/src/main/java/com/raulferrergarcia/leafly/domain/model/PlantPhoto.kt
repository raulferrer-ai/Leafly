package com.raulferrergarcia.leafly.domain.model

import java.util.UUID

/**
 * Una foto del historial de una planta.
 *
 * El historial crece con el tiempo: cada foto guarda su fecha para poder
 * recorrer el crecimiento de la planta de la más reciente a la más antigua.
 * `data` ya viene procesada (JPEG reducido) por [PlantPhotoProcessor].
 */
data class PlantPhoto(
    val id: String = UUID.randomUUID().toString(),
    val plantId: String,
    /** Momento en el que se añadió la foto al historial (epoch millis). */
    val addedAt: Long = System.currentTimeMillis(),
    /** Foto ya procesada, lista para mostrarse y persistirse. */
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlantPhoto) return false

        return id == other.id &&
            plantId == other.plantId &&
            addedAt == other.addedAt &&
            data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + plantId.hashCode()
        result = 31 * result + addedAt.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String =
        "PlantPhoto(id=$id, plantId=$plantId, addedAt=$addedAt, dataSize=${data.size})"
}
