package com.raulferrergarcia.leafly.domain.model

import java.util.UUID

/**
 * Modelo de dominio de una planta.
 *
 * Independiente de Room para mantener el dominio limpio y testeable.
 * Mantiene la misma forma que el modelo de iOS: id, nombre, tipo, fecha de
 * creación, fecha del último riego y foto opcional.
 */
data class Plant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PlantType? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** Último riego registrado. `null` significa que aún no se ha regado. */
    val lastWateredAt: Long? = null,
    /** Foto opcional ya procesada (JPEG). `null` significa que no tiene foto. */
    val photoData: ByteArray? = null,
) {

    // ByteArray usa igualdad por referencia, así que la comparamos por
    // contenido para que el estado y los tests se comporten como se espera.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Plant) return false

        return id == other.id &&
            name == other.name &&
            type == other.type &&
            createdAt == other.createdAt &&
            lastWateredAt == other.lastWateredAt &&
            photoData.contentEquals(other.photoData)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (lastWateredAt?.hashCode() ?: 0)
        result = 31 * result + (photoData?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Plant(id=$id, name=$name, type=$type, createdAt=$createdAt, " +
            "lastWateredAt=$lastWateredAt, hasPhoto=${photoData != null})"
}
