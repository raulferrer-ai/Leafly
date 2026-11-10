package com.raulferrergarcia.leafly.features.plants

import com.raulferrergarcia.leafly.domain.model.Plant

/** Errores que el detalle de planta puede mostrar. */
enum class PlantDetailSaveError {
    SaveFailed,
}

/**
 * Estado inmutable del detalle de una planta.
 */
data class PlantDetailUiState(
    val plant: Plant,
    val isSaving: Boolean = false,
    val saveError: PlantDetailSaveError? = null,
)
