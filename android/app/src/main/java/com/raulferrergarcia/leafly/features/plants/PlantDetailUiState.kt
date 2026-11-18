package com.raulferrergarcia.leafly.features.plants

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import com.raulferrergarcia.leafly.domain.model.WateringLogEntry

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

    // ------------------------------------------------------------------
    // Historial de fotos
    // ------------------------------------------------------------------
    val photos: List<PlantPhoto> = emptyList(),
    val isLoadingPhotos: Boolean = false,
    val isAddingPhoto: Boolean = false,
    val photoErrorMessage: String? = null,

    // ------------------------------------------------------------------
    // Historial de riegos
    // ------------------------------------------------------------------
    val wateringLog: List<WateringLogEntry> = emptyList(),
    val isLoadingWateringLog: Boolean = false,
    val wateringLogErrorMessage: String? = null,
)
