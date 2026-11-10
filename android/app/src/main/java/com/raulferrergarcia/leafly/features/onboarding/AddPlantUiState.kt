package com.raulferrergarcia.leafly.features.onboarding

import com.raulferrergarcia.leafly.domain.model.PlantType

/**
 * Errores que la pantalla puede mostrar. Se modelan como tipos (no como texto)
 * para que el ViewModel no dependa de recursos de Android y sea testeable.
 */
enum class AddPlantError {
    BlankName,
    SaveFailed,
}

/**
 * Avisos calmados relacionados con la foto. También son tipos para que la
 * pantalla decida el texto.
 */
enum class AddPlantPhotoMessage {
    CameraPermissionDenied,
    CameraUnavailable,
    PhotoLoadFailed,
    PhotoCaptureFailed,
}

/**
 * Estado inmutable de la pantalla de añadir planta.
 */
data class AddPlantUiState(
    val name: String = "",
    val selectedType: PlantType? = null,
    /** Foto ya procesada. Solo vive en memoria hasta que se guarda la planta. */
    val photoData: ByteArray? = null,
    val isProcessingPhoto: Boolean = false,
    val photoMessage: AddPlantPhotoMessage? = null,
    val nameError: AddPlantError? = null,
    val saveError: AddPlantError? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)
