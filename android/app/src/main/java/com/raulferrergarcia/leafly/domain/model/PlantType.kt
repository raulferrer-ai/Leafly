package com.raulferrergarcia.leafly.domain.model

/**
 * Tipos básicos de planta disponibles durante el onboarding.
 *
 * El tipo es opcional: la persona puede añadir su planta solo con un nombre.
 * [key] es lo que se guarda en disco; [displayName] es el texto cercano de la UI.
 */
enum class PlantType(
    val key: String,
    val displayName: String,
) {
    MONSTERA(key = "monstera", displayName = "Monstera"),
    FICUS(key = "ficus", displayName = "Ficus"),
    POTHOS(key = "pothos", displayName = "Potos"),
    CACTUS(key = "cactus", displayName = "Cactus"),
    SUCCULENT(key = "succulent", displayName = "Suculenta"),
    FERN(key = "fern", displayName = "Helecho"),
    ORCHID(key = "orchid", displayName = "Orquídea"),
    ;

    companion object {
        /** Devuelve el tipo guardado o `null` si ya no existe. */
        fun fromKey(key: String): PlantType? = entries.firstOrNull { it.key == key }
    }
}
