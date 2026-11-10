package com.raulferrergarcia.leafly.features.plants

import com.raulferrergarcia.leafly.domain.model.Plant

/** Errores que la lista de plantas puede mostrar. */
enum class PlantsLoadError {
    LoadFailed,
}

/**
 * Estado inmutable de la lista de plantas.
 */
data class PlantsUiState(
    val plants: List<Plant> = emptyList(),
    val isLoading: Boolean = true,
    val loadError: PlantsLoadError? = null,
)
